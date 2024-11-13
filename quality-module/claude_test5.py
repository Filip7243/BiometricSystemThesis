import numpy as np
from scipy import signal, ndimage
import cv2
from dataclasses import dataclass
from typing import Tuple, List, Dict
import concurrent.futures
from sklearn.preprocessing import MinMaxScaler


@dataclass
class QualityMetrics:
    snr: float  # Signal-to-Noise Ratio
    cnr: float  # Contrast-to-Noise Ratio
    mean_coherence: float  # Average coherence
    coherence_uniformity: float  # Uniformity of coherence values
    ridge_strength: float  # Overall ridge strength
    ridge_continuity: float  # Ridge continuity measure
    ridge_frequency_uniformity: float  # Uniformity of ridge frequencies
    clarity_score: float  # Overall clarity score
    quality_score: float  # Final combined quality score
    block_quality_map: np.ndarray  # Quality score for each block


class OptimizedFingerprintQuality:
    def __init__(self, block_size: int = 16):
        self.block_size = block_size
        self.sobel_x = np.array([[-1, 0, 1], [-2, 0, 2], [-1, 0, 1]])
        self.sobel_y = np.array([[-1, -2, -1], [0, 0, 0], [1, 2, 1]])

    def compute_block_metrics(self, block: np.ndarray) -> Dict:
        """Compute multiple quality metrics for a single block"""
        if block.size == 0:
            return {}

        # Compute local contrast
        local_contrast = np.std(block)

        # Compute local orientation coherence
        gx = signal.convolve2d(block, self.sobel_x, mode='valid')
        gy = signal.convolve2d(block, self.sobel_y, mode='valid')
        gxx = np.sum(gx * gx)
        gyy = np.sum(gy * gy)
        gxy = np.sum(gx * gy)

        coherence = np.sqrt((gxx - gyy) ** 2 + 4 * gxy ** 2) / (gxx + gyy + 1e-10)

        # Compute ridge frequency metrics
        f_spectrum = np.abs(np.fft.fft2(block))
        f_spectrum = np.fft.fftshift(f_spectrum)

        # Ridge strength from spectrum
        ridge_strength = np.max(f_spectrum) / (np.mean(f_spectrum) + 1e-10)

        return {
            'contrast': local_contrast,
            'coherence': coherence,
            'ridge_strength': ridge_strength
        }

    def compute_snr(self, img: np.ndarray, filtered_img: np.ndarray, mask: np.ndarray) -> float:
        """Compute Signal-to-Noise Ratio"""
        valid_region = (mask > 0)
        if not np.any(valid_region):
            return 0.0

        signal = filtered_img[valid_region]
        noise = img[valid_region] - filtered_img[valid_region]

        signal_power = np.mean(signal ** 2)
        noise_power = np.mean(noise ** 2)

        return 10 * np.log10(signal_power / (noise_power + 1e-10))

    def compute_cnr(self, img: np.ndarray, mask: np.ndarray) -> float:
        """Compute Contrast-to-Noise Ratio"""
        valid_region = (mask > 0)
        if not np.any(valid_region):
            return 0.0

        img_valid = img[valid_region]
        mean_signal = np.mean(img_valid)
        std_noise = np.std(img_valid)

        return mean_signal / (std_noise + 1e-10)

    def compute_ridge_continuity(self, orientations: np.ndarray, mask: np.ndarray) -> np.ndarray:
        """Measure ridge continuity using orientation flow"""
        continuity_map = np.zeros_like(orientations)
        padded_orientations = np.pad(orientations, 1, mode='edge')

        for i in range(1, orientations.shape[0] - 1):
            for j in range(1, orientations.shape[1] - 1):
                if mask[i, j]:
                    # Compute orientation differences with neighbors
                    center = padded_orientations[i, j]
                    neighbors = padded_orientations[i - 1:i + 2, j - 1:j + 2].flatten()

                    # Calculate angular differences
                    diff = np.abs(neighbors - center)
                    diff = np.minimum(diff, np.pi - diff)

                    # High continuity = low angular difference
                    continuity_map[i, j] = 1 - np.mean(diff) / (np.pi / 2)

        return continuity_map

    def compute_block_quality_map(self, img: np.ndarray, orientations: np.ndarray,
                                  frequencies: np.ndarray, mask: np.ndarray) -> np.ndarray:
        """Compute quality score for each block"""
        h, w = img.shape
        block_quality = np.zeros((h // self.block_size, w // self.block_size))

        for i in range(0, h - self.block_size, self.block_size):
            for j in range(0, w - self.block_size, self.block_size):
                if not np.all(mask[i:i + self.block_size, j:j + self.block_size]):
                    continue

                block = img[i:i + self.block_size, j:j + self.block_size]
                metrics = self.compute_block_metrics(block)

                if not metrics:
                    continue

                # Combine metrics into quality score
                quality_score = (
                        0.3 * metrics['coherence'] +
                        0.3 * metrics['ridge_strength'] +
                        0.4 * metrics['contrast']
                )

                block_quality[i // self.block_size, j // self.block_size] = quality_score

        return block_quality

    def evaluate_quality(self, img: np.ndarray, mask: np.ndarray,
                         orientations: np.ndarray, frequencies: np.ndarray,
                         filtered_img: np.ndarray) -> QualityMetrics:
        """Comprehensive quality assessment"""
        # Compute basic quality metrics
        snr = self.compute_snr(img, filtered_img, mask)
        cnr = self.compute_cnr(img, mask)

        snr = 1.0 / (1.0 + np.exp(-snr / 20))  # Sigmoid normalization
        cnr = 1.0 / (1.0 + np.exp(-cnr / 5))  # Sigmoid normalization

        # Compute ridge continuity
        continuity_map = self.compute_ridge_continuity(orientations, mask)
        ridge_continuity = np.mean(continuity_map[mask > 0])

        # Compute block-wise quality map
        block_quality = self.compute_block_quality_map(img, orientations, frequencies, mask)

        if np.any(block_quality > 0):
            block_quality = (block_quality - np.min(block_quality)) / (np.max(block_quality) - np.min(block_quality))

        # Compute frequency uniformity
        valid_frequencies = frequencies[mask > 0]
        freq_uniformity = 1.0 - np.std(valid_frequencies) / (np.mean(valid_frequencies) + 1e-10)

        # Compute coherence statistics
        valid_coherence = block_quality[block_quality > 0]
        mean_coherence = np.mean(valid_coherence) if valid_coherence.size > 0 else 0
        coherence_uniformity = 1.0 - np.std(valid_coherence) / (
                mean_coherence + 1e-10) if valid_coherence.size > 0 else 0

        # Ridge strength from frequency domain
        ft = np.fft.fft2(filtered_img * mask)
        ridge_strength = np.max(np.abs(ft)) / (np.mean(np.abs(ft)) + 1e-10)

        # Clarity score based on local contrast and coherence
        clarity_score = np.mean(block_quality)

        weights = np.array([
            1,  # SNR
            1,  # CNR
            1,  # mean_coherence
            1,  # coherence_uniformity
            1,  # ridge_continuity
            1,  # freq_uniformity
            1  # clarity_score
        ])

        metrics = np.array([
            snr,
            cnr,
            mean_coherence,
            coherence_uniformity,
            ridge_continuity,
            freq_uniformity,
            np.mean(block_quality)
        ])

        print(f'SNR: {snr}')
        print(f'CNR: {cnr}')
        print(f'mean_coherence: {mean_coherence}')
        print(f'coherence_uniformity: {coherence_uniformity}')
        print(f'ridge_continuity: {ridge_continuity}')
        print(f'freq_uniformity: {freq_uniformity}')
        print(f'np.mean(block_quality): {np.mean(block_quality)}')

        quality_score = np.sum(weights * metrics)

        return QualityMetrics(
            snr=snr,
            cnr=cnr,
            mean_coherence=mean_coherence,
            coherence_uniformity=coherence_uniformity,
            ridge_strength=ridge_strength,
            ridge_continuity=ridge_continuity,
            ridge_frequency_uniformity=freq_uniformity,
            clarity_score=clarity_score,
            quality_score=quality_score,
            block_quality_map=block_quality
        )

    def visualize_quality(self, img: np.ndarray, quality_metrics: QualityMetrics) -> None:
        """Visualize quality assessment results"""
        import matplotlib.pyplot as plt

        fig, axes = plt.subplots(2, 2, figsize=(15, 15))

        # Original image
        axes[0, 0].imshow(img, cmap='gray')
        axes[0, 0].set_title('Original Image')

        # Block quality map
        im = axes[0, 1].imshow(quality_metrics.block_quality_map, cmap='jet')
        axes[0, 1].set_title('Block Quality Map')
        plt.colorbar(im, ax=axes[0, 1])

        # Quality metrics text
        axes[1, 0].axis('off')
        metrics_text = (
            f'Quality Score: {quality_metrics.quality_score:.3f}\n'
            f'SNR: {quality_metrics.snr:.2f} dB\n'
            f'CNR: {quality_metrics.cnr:.2f}\n'
            f'Mean Coherence: {quality_metrics.mean_coherence:.3f}\n'
            f'Ridge Continuity: {quality_metrics.ridge_continuity:.3f}\n'
            f'Frequency Uniformity: {quality_metrics.ridge_frequency_uniformity:.3f}\n'
            f'Clarity Score: {quality_metrics.clarity_score:.3f}'
        )
        axes[1, 0].text(0.1, 0.5, metrics_text, fontsize=12)

        # Histogram of block qualities
        valid_qualities = quality_metrics.block_quality_map[quality_metrics.block_quality_map > 0]
        axes[1, 1].hist(valid_qualities.flatten(), bins=50)
        axes[1, 1].set_title('Distribution of Block Qualities')

        plt.tight_layout()
        plt.show()
