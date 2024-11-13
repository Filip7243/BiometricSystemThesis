import numpy as np
import cv2
from scipy import ndimage
from typing import Tuple, Dict
import matplotlib.pyplot as plt


class RidgeOrientationAnalyzer:
    """
    A class to analyze the stability and consistency of ridge orientations
    in fingerprint images.
    """

    def __init__(self, block_size: int = 16, gradient_sigma: float = 1.0,
                 smoothing_sigma: float = 3.0):
        """
        Initialize the ridge orientation analyzer.

        Parameters:
        -----------
        block_size : int
            Size of blocks for local orientation analysis
        gradient_sigma : float
            Sigma for gradient computation smoothing
        smoothing_sigma : float
            Sigma for orientation field smoothing
        """
        self.block_size = block_size
        self.gradient_sigma = gradient_sigma
        self.smoothing_sigma = smoothing_sigma

    def compute_orientation_field(self, image: np.ndarray) -> Tuple[np.ndarray, np.ndarray]:
        """
        Compute the orientation field of the fingerprint image.

        Parameters:
        -----------
        image : np.ndarray
            Input fingerprint image (grayscale)

        Returns:
        --------
        Tuple containing:
            - orientation_map: Ridge orientations in radians
            - coherence_map: Measure of orientation reliability
        """
        # Ensure image is grayscale and normalized
        if len(image.shape) > 2:
            image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
        image = image.astype(np.float32) / 255.0

        # Compute gradients
        gradient_y, gradient_x = np.gradient(image)

        # Smooth gradients
        gradient_x = ndimage.gaussian_filter(gradient_x, self.gradient_sigma)
        gradient_y = ndimage.gaussian_filter(gradient_y, self.gradient_sigma)

        # Compute components for orientation estimation
        gxx = gradient_x * gradient_x
        gyy = gradient_y * gradient_y
        gxy = gradient_x * gradient_y

        # Block processing
        height, width = image.shape
        block_height = height // self.block_size
        block_width = width // self.block_size

        orientation_map = np.zeros((block_height, block_width))
        coherence_map = np.zeros((block_height, block_width))

        for i in range(block_height):
            for j in range(block_width):
                # Extract block coordinates
                start_y = i * self.block_size
                start_x = j * self.block_size
                end_y = start_y + self.block_size
                end_x = start_x + self.block_size

                # Compute average gradients in block
                gxx_block = np.mean(gxx[start_y:end_y, start_x:end_x])
                gyy_block = np.mean(gyy[start_y:end_y, start_x:end_x])
                gxy_block = np.mean(gxy[start_y:end_y, start_x:end_x])

                # Compute orientation
                orientation = 0.5 * np.arctan2(2 * gxy_block, gxx_block - gyy_block)
                orientation_map[i, j] = orientation

                # Compute coherence
                coherence = np.sqrt((gxx_block - gyy_block) ** 2 + 4 * gxy_block ** 2) / \
                            (gxx_block + gyy_block + 1e-10)
                coherence_map[i, j] = coherence

        # Smooth orientation field
        orientation_smooth = self._smooth_orientation_field(orientation_map)
        coherence_map = ndimage.gaussian_filter(coherence_map, self.smoothing_sigma)

        return orientation_smooth, coherence_map

    def _smooth_orientation_field(self, orientation_map: np.ndarray) -> np.ndarray:
        """
        Smooth the orientation field using a vector averaging approach.

        Parameters:
        -----------
        orientation_map : np.ndarray
            Raw orientation field

        Returns:
        --------
        np.ndarray
            Smoothed orientation field
        """
        # Convert orientations to vector form
        phi_x = np.cos(2 * orientation_map)
        phi_y = np.sin(2 * orientation_map)

        # Smooth vector components
        phi_x = ndimage.gaussian_filter(phi_x, self.smoothing_sigma)
        phi_y = ndimage.gaussian_filter(phi_y, self.smoothing_sigma)

        # Convert back to angles
        return 0.5 * np.arctan2(phi_y, phi_x)

    def analyze_orientation_stability(self, image: np.ndarray) -> Dict:
        """
        Analyze the stability of ridge orientations in the fingerprint.

        Parameters:
        -----------
        image : np.ndarray
            Input fingerprint image

        Returns:
        --------
        dict
            Dictionary containing stability metrics:
            - orientation_map: Ridge orientations
            - coherence_map: Local orientation coherence
            - stability_score: Overall stability score
            - local_stability: Local stability measures
            - orientation_changes: Map of orientation changes
        """
        # Compute orientation field and coherence
        orientation_map, coherence_map = self.compute_orientation_field(image)

        # Compute local orientation changes
        grad_y, grad_x = np.gradient(orientation_map)
        orientation_changes = np.sqrt(grad_x ** 2 + grad_y ** 2)

        # Adjust for circular nature of angles
        orientation_changes = np.minimum(orientation_changes, np.pi - orientation_changes)

        # Calculate local stability (inverse of orientation changes, weighted by coherence)
        local_stability = coherence_map * (1 - orientation_changes / np.pi)

        # Calculate overall stability score
        valid_regions = coherence_map > 0.2  # Mask for regions with sufficient coherence
        if np.sum(valid_regions) > 0:
            stability_score = np.mean(local_stability[valid_regions])
        else:
            stability_score = 0.0

        return {
            'orientation_map': orientation_map,
            'coherence_map': coherence_map,
            'stability_score': stability_score,
            'local_stability': local_stability,
            'orientation_changes': orientation_changes
        }

    def visualize_results(self, image: np.ndarray, results: Dict,
                          output_path: str = None) -> np.ndarray:
        """
        Create visualization of orientation stability analysis.

        Parameters:
        -----------
        image : np.ndarray
            Original fingerprint image
        results : Dict
            Results from analyze_orientation_stability
        output_path : str, optional
            Path to save visualization

        Returns:
        --------
        np.ndarray
            Visualization image
        """
        # Create figure with subplots
        fig, axes = plt.subplots(2, 2, figsize=(12, 12))

        # Original image with orientation field overlay
        axes[0, 0].imshow(image, cmap='gray')
        axes[0, 0].set_title('Original Image with Orientation Field')

        # Plot orientation field
        y, x = np.mgrid[0:results['orientation_map'].shape[0],
               0:results['orientation_map'].shape[1]]
        orientation = results['orientation_map']
        coherence = results['coherence_map']

        for i in range(0, len(x), 1):
            for j in range(0, len(y), 1):
                if coherence[j, i] > 0.2:  # Only plot reliable orientations
                    length = 0.5 * self.block_size * coherence[j, i]
                    dx = length * np.cos(orientation[j, i])
                    dy = length * np.sin(orientation[j, i])
                    axes[0, 0].plot([x[j, i] * self.block_size - dx / 2,
                                     x[j, i] * self.block_size + dx / 2],
                                    [y[j, i] * self.block_size - dy / 2,
                                     y[j, i] * self.block_size + dy / 2],
                                    'r-', lw=1)

        # Coherence map
        im1 = axes[0, 1].imshow(results['coherence_map'], cmap='jet')
        axes[0, 1].set_title('Orientation Coherence')
        plt.colorbar(im1, ax=axes[0, 1])

        # Orientation changes
        im2 = axes[1, 0].imshow(results['orientation_changes'], cmap='jet')
        axes[1, 0].set_title('Orientation Changes')
        plt.colorbar(im2, ax=axes[1, 0])

        # Local stability
        im3 = axes[1, 1].imshow(results['local_stability'], cmap='jet')
        axes[1, 1].set_title('Local Stability')
        plt.colorbar(im3, ax=axes[1, 1])

        # Add overall score
        fig.suptitle(f"Overall Stability Score: {results['stability_score']:.3f}",
                     fontsize=14)

        if output_path:
            plt.savefig(output_path, bbox_inches='tight')
            plt.close()
            return cv2.imread(output_path)

        return fig
