from pathlib import Path

from matplotlib import pyplot as plt

import img_orientation
from img_orientation import estimate_orientation
import numpy as np
import cv2
import utils


def detect_minutiae(orientation_map, threshold=0.5):
    rows, cols = orientation_map.shape
    minutiae_points = []

    # Define minutiae detection by searching local structures (crossings and bifurcations)
    for i in range(1, rows - 1):
        for j in range(1, cols - 1):
            # Detect local structure and classify minutiae points
            neighbor_orientations = [
                orientation_map[i - 1, j - 1], orientation_map[i - 1, j], orientation_map[i - 1, j + 1],
                orientation_map[i, j - 1], orientation_map[i, j + 1],
                orientation_map[i + 1, j - 1], orientation_map[i + 1, j], orientation_map[i + 1, j + 1]
            ]

            # Calculate the number of distinct orientations
            orientation_diff = np.abs(np.array(neighbor_orientations) - orientation_map[i, j])
            distinct_orientation_count = np.sum(orientation_diff > threshold)

            # Detect bifurcations: More than 2 distinct neighbors
            if distinct_orientation_count > 2:
                minutiae_points.append(('Bifurcation', i, j))
            # Detect cores or deltas: Based on the angle change across blocks
            elif distinct_orientation_count == 1:
                minutiae_points.append(('Core/Delta', i, j))

    return minutiae_points


def visualize_minutiae(image, minutiae_points):
    plt.imshow(image, cmap='gray')
    for point in minutiae_points:
        type, x, y = point
        color = 'r' if type == 'Bifurcation' else 'b'
        plt.scatter(y, x, color=color, s=30, marker='x')
    plt.title('Minutiae Detection: Bifurcations (Red), Cores/Deltas (Blue)')
    plt.axis('off')
    plt.show()


folder = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\analized_data')
quality_folders = [f for f in folder.iterdir() if f.is_dir()]

for quality_folder in quality_folders[:]:
    files = list(quality_folder.glob('*.png'))

    for file in files[4:7]:
        print(file)
        image = utils.read_image(file)
        mask = utils.segment_fingerprint(image)
        image = utils.normalize_image(image)

        orientation, _ = img_orientation.estimate_orientation(image, _interpolate=True)
        resized_orientation = cv2.resize(mask, (orientation.shape[1] // 16, orientation.shape[0] // 16),
                                         interpolation=cv2.INTER_NEAREST)
        minutiae = detect_minutiae(resized_orientation)
        visualize_minutiae(image, minutiae)
