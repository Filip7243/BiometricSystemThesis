from pathlib import Path

import numpy as np
import cv2
from dataclasses import dataclass
from typing import Tuple, Dict, List

import utils


@dataclass
class PositionMetrics:
    center_offset: Tuple[float, float]  # (x, y) offset from image center
    coverage_ratio: float  # ratio of fingerprint area to image area
    position_quality: float  # overall position quality score
    is_centered: bool  # whether fingerprint is well-centered
    rotation_angle: float  # estimated rotation angle
    bounding_box: Tuple[int, int, int, int]  # x, y, w, h
    segmentation_mask: np.ndarray  # binary mask of fingerprint region


class FingerprintPositionAnalyzer:
    def __init__(self,
                 min_coverage: float = 0.2,
                 max_coverage: float = 0.8,
                 center_threshold: float = 0.15):
        """
        Initialize analyzer with configurable thresholds.

        Args:
            min_coverage: Minimum acceptable fingerprint coverage ratio
            max_coverage: Maximum acceptable fingerprint coverage ratio
            center_threshold: Maximum acceptable center offset ratio
        """
        self.min_coverage = min_coverage
        self.max_coverage = max_coverage
        self.center_threshold = center_threshold

    def segment_fingerprint(self, image: np.ndarray) -> np.ndarray:
        """
        Segment fingerprint from background.

        Args:
            image: Input grayscale image

        Returns:
            Binary mask where fingerprint region is 1
        """
        # Normalize image
        normalized = cv2.normalize(image, None, 0, 255, cv2.NORM_MINMAX)

        # Apply variance-based segmentation
        variance = np.zeros_like(image, dtype=np.float32)
        kernel_size = 15
        mean = cv2.blur(normalized, (kernel_size, kernel_size))
        mean_sq = cv2.blur(normalized * normalized, (kernel_size, kernel_size))
        variance = mean_sq - mean * mean

        # Threshold variance to get fingerprint region
        _, mask = cv2.threshold(variance, 100, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
        mask = mask.astype(np.uint8)

        # Clean up mask using morphological operations
        kernel = np.ones((5, 5), np.uint8)
        mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel)
        mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel)

        return mask

    def find_center_of_mass(self, mask: np.ndarray) -> Tuple[float, float]:
        """
        Calculate center of mass of the fingerprint region.

        Args:
            mask: Binary mask of fingerprint region

        Returns:
            (x, y) coordinates of center of mass
        """
        moments = cv2.moments(mask)
        if moments['m00'] != 0:
            cx = moments['m10'] / moments['m00']
            cy = moments['m01'] / moments['m00']
        else:
            cx, cy = mask.shape[1] / 2, mask.shape[0] / 2
        return cx, cy

    def calculate_rotation_angle(self, mask: np.ndarray) -> float:
        """
        Estimate fingerprint rotation angle using PCA.

        Args:
            mask: Binary mask of fingerprint region

        Returns:
            Estimated rotation angle in degrees
        """
        # Get coordinates of fingerprint pixels
        y, x = np.nonzero(mask)
        coords = np.column_stack((x, y))

        if len(coords) < 2:
            return 0.0

        # Calculate PCA
        mean = np.mean(coords, axis=0)
        coords_centered = coords - mean
        cov = np.cov(coords_centered.T)
        eigenvalues, eigenvectors = np.linalg.eig(cov)

        # Get angle of principal component
        principal_vector = eigenvectors[np.argmax(eigenvalues)]
        angle = np.arctan2(principal_vector[1], principal_vector[0])
        return np.degrees(angle)

    def get_bounding_box(self, mask: np.ndarray) -> Tuple[int, int, int, int]:
        """
        Get bounding box of fingerprint region.

        Args:
            mask: Binary mask of fingerprint region

        Returns:
            (x, y, width, height) of bounding box
        """
        contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        if contours:
            largest_contour = max(contours, key=cv2.contourArea)
            return cv2.boundingRect(largest_contour)
        return (0, 0, mask.shape[1], mask.shape[0])

    def analyze_position(self, image: np.ndarray) -> PositionMetrics:
        """
        Analyze fingerprint position in image.

        Args:
            image: Input grayscale image

        Returns:
            PositionMetrics object containing analysis results
        """
        # Segment fingerprint
        mask = self.segment_fingerprint(image)

        # Find center of mass
        fp_center_x, fp_center_y = self.find_center_of_mass(mask)

        # Calculate offset from image center
        img_center_x, img_center_y = image.shape[1] / 2, image.shape[0] / 2
        offset_x = (fp_center_x - img_center_x) / img_center_x
        offset_y = (fp_center_y - img_center_y) / img_center_y

        # Calculate coverage ratio
        coverage = np.sum(mask > 0) / mask.size

        # Get bounding box
        bbox = self.get_bounding_box(mask)

        # Calculate rotation angle
        rotation = self.calculate_rotation_angle(mask)

        # Calculate position quality score
        center_dist = np.sqrt(offset_x ** 2 + offset_y ** 2)
        is_centered = center_dist <= self.center_threshold

        coverage_score = 1.0
        if coverage < self.min_coverage:
            coverage_score = coverage / self.min_coverage
        elif coverage > self.max_coverage:
            coverage_score = 1.0 - (coverage - self.max_coverage) / (1.0 - self.max_coverage)

        position_quality = (
                (1.0 - min(center_dist, 1.0)) * 0.6 +  # Center weight
                coverage_score * 0.4  # Coverage weight
        )

        return PositionMetrics(
            center_offset=(offset_x, offset_y),
            coverage_ratio=coverage,
            position_quality=position_quality,
            is_centered=is_centered,
            rotation_angle=rotation,
            bounding_box=bbox,
            segmentation_mask=mask
        )

    def visualize_analysis(self, image: np.ndarray, metrics: PositionMetrics) -> np.ndarray:
        """
        Visualize position analysis results.

        Args:
            image: Original grayscale image
            metrics: PositionMetrics object

        Returns:
            Visualization image with marked analysis results
        """
        # Convert to RGB
        vis_img = cv2.cvtColor(image, cv2.COLOR_GRAY2RGB)

        # Draw fingerprint boundary
        contours, _ = cv2.findContours(metrics.segmentation_mask,
                                       cv2.RETR_EXTERNAL,
                                       cv2.CHAIN_APPROX_SIMPLE)
        cv2.drawContours(vis_img, contours, -1, (0, 255, 0), 2)

        # Draw bounding box
        x, y, w, h = metrics.bounding_box
        cv2.rectangle(vis_img, (x, y), (x + w, y + h), (255, 0, 0), 2)

        # Draw centers
        img_center = (image.shape[1] // 2, image.shape[0] // 2)
        fp_center = (int(img_center[0] * (1 + metrics.center_offset[0])),
                     int(img_center[1] * (1 + metrics.center_offset[1])))

        # Image center in red
        cv2.circle(vis_img, img_center, 5, (0, 0, 255), -1)
        # Fingerprint center in green
        cv2.circle(vis_img, fp_center, 5, (0, 255, 0), -1)

        # Draw line between centers
        cv2.line(vis_img, img_center, fp_center, (255, 0, 0), 2)

        # Add text information
        info_text = [
            f"Quality: {metrics.position_quality:.2f}",
            f"Coverage: {metrics.coverage_ratio:.1%}",
            f"Centered: {'Yes' if metrics.is_centered else 'No'}",
            f"Rotation: {metrics.rotation_angle:.1f}°"
        ]

        for i, text in enumerate(info_text):
            cv2.putText(vis_img, text, (10, 30 + i * 30),
                        cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)

        return vis_img


folder = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\analized_data')
quality_folders = [f for f in folder.iterdir() if f.is_dir()]
analyzer = FingerprintPositionAnalyzer()

for quality_folder in quality_folders[:]:
    files = list(quality_folder.glob('*.png'))

    for file in files[4:7]:
        print(file)
        image = utils.read_image(file)

        metrics = analyzer.analyze_position(image)

        # Create visualization
        vis_image = analyzer.visualize_analysis(image, metrics)

        # Print results
        print(f"Center offset: ({metrics.center_offset[0]:.2f}, {metrics.center_offset[1]:.2f})")
        print(f"Coverage ratio: {metrics.coverage_ratio:.1%}")
        print(f"Position quality: {metrics.position_quality:.2f}")
        print(f"Is centered: {metrics.is_centered}")
        print(f"Rotation angle: {metrics.rotation_angle:.1f}°")

        # Show visualization
        cv2.imshow('Position Analysis', vis_image)
        cv2.waitKey(0)
        cv2.destroyAllWindows()
