import numpy as np
import cv2
from scipy import ndimage
from typing import Dict, Tuple
from dataclasses import dataclass
import matplotlib.pyplot as plt
from sklearn.mixture import GaussianMixture


@dataclass
class SegmentationParams:
    """Parameters for fingerprint segmentation analysis."""
    block_size: int = 16
    variance_threshold: float = 0.001
    coherence_threshold: float = 0.3
    smooth_kernel_size: int = 7
    gmm_components: int = 2
    min_block_mean: float = 0.05
    max_block_mean: float = 0.95


class FingerprintSegmenter:
    """
    A class for analyzing and evaluating fingerprint image segmentation quality.
    Uses multiple features including variance, coherence, and intensity for robust segmentation.
    """

    def __init__(self, params: SegmentationParams = None):
        """
        Initialize the segmenter with given parameters.

        Parameters:
        -----------
        params : SegmentationParams
            Parameters for segmentation analysis
        """
        self.params = params or SegmentationParams()

    def compute_block_features(self, image: np.ndarray) -> Tuple[np.ndarray, np.ndarray, np.ndarray]:
        """
        Compute block-wise features for segmentation.

        Parameters:
        -----------
        image : np.ndarray
            Input fingerprint image (grayscale)

        Returns:
        --------
        Tuple containing:
            - variance_map: Local variance in blocks
            - coherence_map: Local orientation coherence
            - mean_map: Local mean intensity
        """
        # Ensure image is normalized
        image = image.astype(np.float32) / 255.0

        # Initialize feature maps
        blocks_y = image.shape[0] // self.params.block_size
        blocks_x = image.shape[1] // self.params.block_size
        variance_map = np.zeros((blocks_y, blocks_x))
        coherence_map = np.zeros((blocks_y, blocks_x))
        mean_map = np.zeros((blocks_y, blocks_x))

        # Compute gradients for coherence
        gradient_y, gradient_x = np.gradient(image)
        gradient_xx = gradient_x * gradient_x
        gradient_yy = gradient_y * gradient_y
        gradient_xy = gradient_x * gradient_y

        # Process each block
        for i in range(blocks_y):
            for j in range(blocks_x):
                # Block coordinates
                block_y = i * self.params.block_size
                block_x = j * self.params.block_size
                block = image[block_y:block_y + self.params.block_size,
                        block_x:block_x + self.params.block_size]

                # Compute block variance
                variance_map[i, j] = np.var(block)

                # Compute block mean
                mean_map[i, j] = np.mean(block)

                # Compute block coherence
                gxx = np.mean(gradient_xx[block_y:block_y + self.params.block_size,
                              block_x:block_x + self.params.block_size])
                gyy = np.mean(gradient_yy[block_y:block_y + self.params.block_size,
                              block_x:block_x + self.params.block_size])
                gxy = np.mean(gradient_xy[block_y:block_y + self.params.block_size,
                              block_x:block_x + self.params.block_size])

                coherence = np.sqrt((gxx - gyy) ** 2 + 4 * gxy ** 2) / (gxx + gyy + 1e-10)
                coherence_map[i, j] = coherence

        return variance_map, coherence_map, mean_map

    def segment_fingerprint(self, image: np.ndarray) -> Dict:
        """
        Perform fingerprint segmentation and quality analysis.

        Parameters:
        -----------
        image : np.ndarray
            Input fingerprint image

        Returns:
        --------
        dict
            Dictionary containing segmentation results and quality metrics
        """
        # Ensure grayscale
        if len(image.shape) > 2:
            image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

        # Compute block features
        variance_map, coherence_map, mean_map = self.compute_block_features(image)

        # Create feature vector for GMM
        features = np.column_stack((
            variance_map.ravel(),
            coherence_map.ravel(),
            mean_map.ravel()
        ))

        # Fit GMM for classification
        gmm = GaussianMixture(n_components=self.params.gmm_components,
                              covariance_type='full',
                              random_state=42)
        labels = gmm.fit_predict(features)

        # Reshape labels to block dimensions
        mask = labels.reshape(variance_map.shape)

        # Determine foreground label (higher variance usually indicates foreground)
        fg_label = 0 if np.mean(variance_map[mask == 0]) > np.mean(variance_map[mask == 1]) else 1
        mask = (mask == fg_label)

        # Apply morphological operations to clean up mask
        mask = ndimage.binary_opening(mask)
        mask = ndimage.binary_closing(mask)

        # Resize mask to original image size
        full_mask = cv2.resize(mask.astype(np.uint8),
                               (image.shape[1], image.shape[0]),
                               interpolation=cv2.INTER_NEAREST)

        # Compute quality metrics
        quality_metrics = self._compute_quality_metrics(full_mask, variance_map,
                                                        coherence_map, mean_map)

        return {
            'segmentation_mask': full_mask,
            'block_mask': mask,
            'variance_map': variance_map,
            'coherence_map': coherence_map,
            'mean_map': mean_map,
            'quality_metrics': quality_metrics
        }

    def _compute_quality_metrics(self, mask: np.ndarray, variance_map: np.ndarray,
                                 coherence_map: np.ndarray, mean_map: np.ndarray) -> Dict:
        """
        Compute quality metrics for the segmentation.

        Parameters:
        -----------
        mask : np.ndarray
            Segmentation mask
        variance_map : np.ndarray
            Block variance map
        coherence_map : np.ndarray
            Block coherence map
        mean_map : np.ndarray
            Block mean intensity map

        Returns:
        --------
        dict
            Dictionary of quality metrics
        """
        # Compute foreground area ratio
        foreground_ratio = np.mean(mask)

        # Compute mean variance in foreground
        block_mask = cv2.resize(mask, (variance_map.shape[1], variance_map.shape[0]),
                                interpolation=cv2.INTER_NEAREST)
        fg_variance = np.mean(variance_map[block_mask > 0])

        # Compute mean coherence in foreground
        fg_coherence = np.mean(coherence_map[block_mask > 0])

        # Compute contrast between foreground and background
        fg_intensity = np.mean(mean_map[block_mask > 0])
        bg_intensity = np.mean(mean_map[block_mask == 0]) if np.any(block_mask == 0) else 0
        contrast = abs(fg_intensity - bg_intensity)

        # Compute edge strength along boundary using Sobel instead of Laplacian
        mask_float = mask.astype(np.float32)
        sobelx = cv2.Sobel(mask_float, cv2.CV_32F, 1, 0, ksize=3)
        sobely = cv2.Sobel(mask_float, cv2.CV_32F, 0, 1, ksize=3)
        edge_strength = np.mean(np.sqrt(sobelx ** 2 + sobely ** 2))

        # Compute overall quality score
        quality_score = np.clip(
            0.3 * fg_variance / self.params.variance_threshold +
            0.3 * fg_coherence / self.params.coherence_threshold +
            0.2 * contrast +
            0.2 * edge_strength,
            0, 1
        )

        return {
            'quality_score': quality_score,
            'foreground_ratio': foreground_ratio,
            'foreground_variance': fg_variance,
            'foreground_coherence': fg_coherence,
            'contrast': contrast,
            'edge_strength': edge_strength
        }

    def visualize_results(self, image: np.ndarray, results: Dict,
                          output_path: str = None) -> np.ndarray:
        """
        Create visualization of segmentation results.

        Parameters:
        -----------
        image : np.ndarray
            Original fingerprint image
        results : Dict
            Results from segment_fingerprint
        output_path : str, optional
            Path to save visualization

        Returns:
        --------
        np.ndarray
            Visualization image
        """
        # Create figure with subplots
        fig, axes = plt.subplots(2, 3, figsize=(15, 10))

        # Original image with segmentation overlay
        axes[0, 0].imshow(image, cmap='gray')
        mask_overlay = np.zeros_like(image)
        mask_overlay[results['segmentation_mask'] > 0] = 1
        axes[0, 0].imshow(mask_overlay, alpha=0.3, cmap='Reds')
        axes[0, 0].set_title('Original with Segmentation')

        # Variance map
        im1 = axes[0, 1].imshow(results['variance_map'], cmap='jet')
        axes[0, 1].set_title('Block Variance')
        plt.colorbar(im1, ax=axes[0, 1])

        # Coherence map
        im2 = axes[0, 2].imshow(results['coherence_map'], cmap='jet')
        axes[0, 2].set_title('Block Coherence')
        plt.colorbar(im2, ax=axes[0, 2])

        # Mean intensity map
        im3 = axes[1, 0].imshow(results['mean_map'], cmap='jet')
        axes[1, 0].set_title('Block Mean Intensity')
        plt.colorbar(im3, ax=axes[1, 0])

        # Final segmentation mask
        axes[1, 1].imshow(results['segmentation_mask'], cmap='gray')
        axes[1, 1].set_title('Segmentation Mask')

        # Quality metrics text
        axes[1, 2].axis('off')
        metrics = results['quality_metrics']
        metrics_text = (
            f"Quality Score: {metrics['quality_score']:.3f}\n"
            f"Foreground Ratio: {metrics['foreground_ratio']:.3f}\n"
            f"Foreground Variance: {metrics['foreground_variance']:.3f}\n"
            f"Foreground Coherence: {metrics['foreground_coherence']:.3f}\n"
            f"FG/BG Contrast: {metrics['contrast']:.3f}\n"
            f"Edge Strength: {metrics['edge_strength']:.3f}"
        )
        axes[1, 2].text(0.1, 0.5, metrics_text, fontsize=10,
                        verticalalignment='center')

        plt.tight_layout()

        if output_path:
            plt.savefig(output_path, bbox_inches='tight')
            plt.close()
            return cv2.imread(output_path)

        return fig
