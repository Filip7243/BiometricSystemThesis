from dataclasses import dataclass
from typing import Dict, Tuple

import cv2
import matplotlib.pyplot as plt
import numpy as np
from scipy.signal import wiener
from skimage.restoration import estimate_sigma


@dataclass
class NoiseParams:
    """Parameters for noise analysis."""
    block_size: int = 16
    gabor_kernel_size: int = 16
    gabor_freq: float = 1 / 10.0  # Typical ridge frequency
    gabor_sigma: float = 4.0
    gabor_orientations: int = 8
    noise_threshold: float = 0.15
    snr_threshold: float = 10.0


class FingerprintNoiseAnalyzer:
    """
    A class for analyzing noise levels in fingerprint images using multiple
    approaches including frequency analysis, local variance, and signal-to-noise ratio.
    """

    def __init__(self, params: NoiseParams = None):
        """
        Initialize the noise analyzer with given parameters.

        Parameters:
        -----------
        params : NoiseParams
            Parameters for noise analysis
        """
        self.params = params or NoiseParams()
        self.gabor_filters = self._create_gabor_filters()

    def _create_gabor_filters(self) -> list:
        """
        Create a bank of Gabor filters for different orientations.

        Returns:
        --------
        list
            List of Gabor filters
        """
        filters = []
        for theta in np.arange(0, np.pi, np.pi / self.params.gabor_orientations):
            kernel = cv2.getGaborKernel(
                (self.params.gabor_kernel_size, self.params.gabor_kernel_size),
                self.params.gabor_sigma,
                theta,
                1.0 / self.params.gabor_freq,
                0.5,  # Spatial aspect ratio
                0,  # Phase offset
                ktype=cv2.CV_32F
            )
            kernel /= kernel.sum()  # Normalize
            filters.append(kernel)
        return filters

    def estimate_noise(self, image: np.ndarray) -> Dict:
        """
        Perform comprehensive noise analysis on the fingerprint image.

        Parameters:
        -----------
        image : np.ndarray
            Input fingerprint image

        Returns:
        --------
        dict
            Dictionary containing noise analysis results
        """
        # Ensure grayscale and float
        if len(image.shape) > 2:
            image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        image_float = image.astype(np.float32) / 255.0

        # 1. Estimate global noise level using wavelets
        sigma_noise = estimate_sigma(image_float)

        # 2. Local noise analysis
        local_noise = self._analyze_local_noise(image_float)

        # 3. Frequency domain noise analysis
        freq_noise = self._analyze_frequency_noise(image_float)

        # 4. Signal-to-noise ratio analysis
        snr_map, mean_snr = self._compute_snr(image_float)

        # 5. Residual noise after Gabor filtering
        residual_noise = self._analyze_residual_noise(image_float)

        # Compute overall noise score
        noise_score = self._compute_noise_score(
            sigma_noise,
            local_noise['mean_noise'],
            freq_noise['noise_power'],
            mean_snr,
            residual_noise['mean_residual']
        )

        print(f'MEAN: {mean_snr}')

        return {
            'noise_score': noise_score,
            'global_noise': sigma_noise,
            'local_noise': local_noise,
            'frequency_noise': freq_noise,
            'snr_analysis': {'snr_map': snr_map, 'mean_snr': mean_snr},
            'residual_noise': residual_noise
        }

    def _analyze_local_noise(self, image: np.ndarray) -> Dict:
        """
        Analyze noise at local block level.
        """
        blocks_y = image.shape[0] // self.params.block_size
        blocks_x = image.shape[1] // self.params.block_size
        noise_map = np.zeros((blocks_y, blocks_x))

        for i in range(blocks_y):
            for j in range(blocks_x):
                block = image[i * self.params.block_size:(i + 1) * self.params.block_size,
                        j * self.params.block_size:(j + 1) * self.params.block_size]

                # Apply Wiener filter to estimate noise
                filtered_block = wiener(block, (3, 3))
                noise = block - filtered_block
                noise_map[i, j] = np.std(noise)

        return {
            'noise_map': noise_map,
            'mean_noise': np.mean(noise_map),
            'max_noise': np.max(noise_map)
        }

    def _analyze_frequency_noise(self, image: np.ndarray) -> Dict:
        """
        Analyze noise in frequency domain.
        """
        # Compute FFT
        f_transform = np.fft.fft2(image)
        f_shift = np.fft.fftshift(f_transform)
        magnitude_spectrum = np.abs(f_shift)

        # Create frequency mask (high-pass filter)
        rows, cols = image.shape
        center_row, center_col = rows // 2, cols // 2
        mask = np.ones((rows, cols))
        mask[center_row - 30:center_row + 30, center_col - 30:center_col + 30] = 0

        # Estimate noise power in high frequency regions
        noise_power = np.mean(magnitude_spectrum * mask)

        return {
            'magnitude_spectrum': magnitude_spectrum,
            'noise_power': noise_power
        }

    def _compute_snr(self, image: np.ndarray) -> Tuple[np.ndarray, float]:
        """
        Compute local signal-to-noise ratio.
        """
        # Apply Gabor filtering to estimate signal
        signal = np.zeros_like(image)
        for kernel in self.gabor_filters:
            filtered = cv2.filter2D(image, -1, kernel)
            signal = np.maximum(signal, filtered)

        # Estimate noise as difference from signal
        noise = np.abs(image - signal)

        # Compute local SNR
        blocks_y = image.shape[0] // self.params.block_size
        blocks_x = image.shape[1] // self.params.block_size
        snr_map = np.zeros((blocks_y, blocks_x))

        for i in range(blocks_y):
            for j in range(blocks_x):
                signal_block = signal[i * self.params.block_size:(i + 1) * self.params.block_size,
                               j * self.params.block_size:(j + 1) * self.params.block_size]
                noise_block = noise[i * self.params.block_size:(i + 1) * self.params.block_size,
                              j * self.params.block_size:(j + 1) * self.params.block_size]

                signal_power = np.mean(signal_block ** 2)
                noise_power = np.mean(noise_block ** 2) + 1e-10
                snr_map[i, j] = 10 * np.log10(signal_power / noise_power)

        return snr_map, np.mean(snr_map)

    def _analyze_residual_noise(self, image: np.ndarray) -> Dict:
        """
        Analyze residual noise after ridge pattern extraction.
        """
        # Apply directional filtering
        filtered_image = np.zeros_like(image)
        for kernel in self.gabor_filters:
            filtered = cv2.filter2D(image, -1, kernel)
            filtered_image = np.maximum(filtered_image, filtered)

        # Compute residual
        residual = np.abs(image - filtered_image)

        # Analyze residual statistics
        return {
            'residual_map': residual,
            'mean_residual': np.mean(residual),
            'std_residual': np.std(residual)
        }

    def _compute_noise_score(self, global_noise: float, local_noise: float,
                             freq_noise: float, snr: float, residual: float) -> float:
        """
        Compute overall noise score combining different metrics.
        """
        # Normalize and combine metrics
        norm_global = np.clip(global_noise / self.params.noise_threshold, 0, 1)
        norm_local = np.clip(local_noise / self.params.noise_threshold, 0, 1)
        norm_freq = np.clip(freq_noise / (self.params.noise_threshold * 100), 0, 1)
        norm_snr = np.clip(snr / self.params.snr_threshold, 0, 1)
        norm_residual = np.clip(residual / self.params.noise_threshold, 0, 1)

        # Weighted combination (lower score means less noise)
        noise_score = (0.2 * norm_global +
                       0.2 * norm_local +
                       0.2 * norm_freq +
                       0.2 * (1 - norm_snr) +
                       0.2 * norm_residual)

        return 1 - noise_score  # Convert to quality score (higher is better)

    def visualize_results(self, image: np.ndarray, results: Dict,
                          output_path: str = None) -> np.ndarray:
        """
        Create visualization of noise analysis results.
        """
        fig, axes = plt.subplots(2, 3, figsize=(15, 10))

        # Original image
        axes[0, 0].imshow(image, cmap='gray')
        axes[0, 0].set_title('Original Image')

        # Local noise map
        im1 = axes[0, 1].imshow(results['local_noise']['noise_map'], cmap='jet')
        axes[0, 1].set_title('Local Noise Map')
        plt.colorbar(im1, ax=axes[0, 1])

        # SNR map
        im2 = axes[0, 2].imshow(results['snr_analysis']['snr_map'], cmap='jet')
        axes[0, 2].set_title('Signal-to-Noise Ratio Map')
        plt.colorbar(im2, ax=axes[0, 2])

        # Frequency spectrum
        axes[1, 0].imshow(np.log(1 + np.abs(results['frequency_noise']['magnitude_spectrum'])),
                          cmap='gray')
        axes[1, 0].set_title('Frequency Spectrum')

        # Residual noise
        im3 = axes[1, 1].imshow(results['residual_noise']['residual_map'], cmap='jet')
        axes[1, 1].set_title('Residual Noise')
        plt.colorbar(im3, ax=axes[1, 1])

        # Metrics text
        axes[1, 2].axis('off')
        metrics_text = (
            f"Overall Quality Score: {results['noise_score']:.3f}\n"
            f"Global Noise Level: {results['global_noise']:.3f}\n"
            f"Mean Local Noise: {results['local_noise']['mean_noise']:.3f}\n"
            f"Mean SNR (dB): {results['snr_analysis']['mean_snr']:.1f}\n"
            f"Mean Residual: {results['residual_noise']['mean_residual']:.3f}"
        )
        axes[1, 2].text(0.1, 0.5, metrics_text, fontsize=10,
                        verticalalignment='center')

        plt.tight_layout()

        if output_path:
            plt.savefig(output_path, bbox_inches='tight')
            plt.close()
            return cv2.imread(output_path)

        return fig
