from pathlib import Path

import cv2
import numpy as np
from matplotlib import pyplot as plt
from scipy.ndimage.filters import gaussian_filter
from scipy.stats import norm
import img_orientation
import utils


def assess_orientation_consistency(image):
    """
    Assess the consistency of the local ridge orientations within the fingerprint image.

    Parameters:
    image (numpy.ndarray): The input fingerprint image.

    Returns:
    dict: A dictionary containing the orientation consistency scores.
    """
    quality_scores = {}

    # Compute the orientation map
    orientation_map, _ = img_orientation.estimate_orientation(image, _interpolate=True)
    # orientation_map = get_orientation_map(image)

    # Analyze the orientation map
    orientation_consistency = analyze_orientation_map(orientation_map)
    quality_scores['orientation_consistency'] = orientation_consistency

    # Analyze the orientation error PDF
    orientation_error_pdf = analyze_orientation_error_pdf(orientation_map)
    quality_scores['orientation_error_pdf'] = orientation_error_pdf

    return quality_scores


def get_orientation_map(image):
    """
    Compute the gradient-based image orientation with Gaussian smoothing.

    Parameters:
    image (numpy.ndarray): The input fingerprint image.

    Returns:
    numpy.ndarray: A 2D numpy array containing the orientation values in the range [-pi/2, pi/2].
    """
    # orientation_map = gaussian_filter(image, sigma=1)

    # Compute image gradients
    dx, dy = np.gradient(image)

    # Compute orientation angle
    orientation_angle = np.arctan2(dy, dx) / 2

    # Apply Gaussian smoothing to the orientation angle
    orientation_map = gaussian_filter(orientation_angle, sigma=1)

    orientation_map = (orientation_map + np.pi * 0.5) % np.pi

    print(f'orient: {np.min(orientation_map)}, {np.max(orientation_map)}')

    return orientation_map


def analyze_orientation_map(orientation_map):
    """
    Analyze the consistency of the local ridge orientations within the orientation map.

    Parameters:
    orientation_map (numpy.ndarray): The orientation map computed using `get_orientation_map`.

    Returns:
    float: The orientation consistency score.
    """
    block_size = 16
    orientation_consistency = 0.0

    # Iterate over image blocks
    for y in range(0, orientation_map.shape[0], block_size):
        for x in range(0, orientation_map.shape[1], block_size):
            block_orientation = orientation_map[y:y + block_size, x:x + block_size]

            # Compute the standard deviation of the orientation angles within the block
            block_std = np.std(block_orientation)
            orientation_consistency += 1 - block_std / (np.pi / 2)

    # Normalize the orientation consistency score
    orientation_consistency /= (orientation_map.shape[0] // block_size) * (orientation_map.shape[1] // block_size)

    return orientation_consistency


def analyze_orientation_error_pdf(orientation_map):
    """
    Analyze the probability density function (PDF) of orientation errors within the orientation map.

    Parameters:
    orientation_map (numpy.ndarray): The orientation map computed using `get_orientation_map`.

    Returns:
    float: The orientation error PDF score.
    """
    orientation_errors = get_orientation_errors(orientation_map)
    orientation_error_pdf = norm.pdf(orientation_errors, loc=0, scale=np.pi / 4).mean()
    return orientation_error_pdf


def visualize_orientation_consistency(image, quality_scores):
    """
    Visualize the orientation consistency and orientation error PDF for the fingerprint image.

    Parameters:
    image (numpy.ndarray): The input fingerprint image.
    quality_scores (dict): A dictionary containing the orientation consistency scores.
    """
    orientation_map = get_orientation_map(image)
    # orientation_map, _ = img_orientation.estimate_orientation(image, _interpolate=True)
    orientation_consistency = quality_scores['orientation_consistency']
    orientation_error_pdf = quality_scores['orientation_error_pdf']

    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))

    # Orientation map visualization
    ax1.imshow(orientation_map, cmap='jet')
    ax1.set_title('Orientation Map')
    ax1.set_xlabel('X')
    ax1.set_ylabel('Y')

    # Orientation consistency histogram
    orientation_errors = get_orientation_errors(orientation_map)
    ax2.hist(orientation_errors, bins=30, density=True)
    x = np.linspace(-np.pi / 2, np.pi / 2, 100)
    ax2.plot(x, norm.pdf(x, loc=0, scale=np.pi / 4), 'r-', linewidth=2)
    ax2.set_title(f'Orientation Error PDF (Consistency: {orientation_consistency:.2f})')
    ax2.set_xlabel('Orientation Error (radians)')
    ax2.set_ylabel('Probability Density')

    plt.suptitle(f'Fingerprint Image Quality Visualization')
    plt.show()


def get_orientation_errors(orientation_map):
    """
    Compute the orientation errors within the orientation map.

    Parameters:
    orientation_map (numpy.ndarray): The orientation map computed using `get_orientation_map`.

    Returns:
    numpy.ndarray: A 1D numpy array containing the orientation errors.
    """
    block_size = 16
    orientation_errors = []

    for y in range(0, orientation_map.shape[0], block_size):
        for x in range(0, orientation_map.shape[1], block_size):
            block_orientation = orientation_map[y:y + block_size, x:x + block_size]
            block_mean = np.mean(block_orientation)
            block_errors = np.abs(block_orientation - block_mean)
            orientation_errors.extend(block_errors.ravel())

    return np.array(orientation_errors)


excellent = r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002_and_cross\DB1_B_PNGS\classify\excellent\5.png'
good = r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002_and_cross\DB1_B_PNGS\classify\good\3.png'
very_good = r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002_and_cross\DB1_B_PNGS\classify\very_good\0.png'
very_poor = r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\analized_data\VeryPoor\27.png'
fair = r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002_and_cross\DB2_B_PNGS\classify\fair\79.png'
fair2 = r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002_and_cross\DB4_B_PNGS\classify\fair\67.png'


folder = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\analized_data')
quality_folders = [f for f in folder.iterdir() if f.is_dir()]

for quality_folder in quality_folders:
    quality_class = quality_folder.name
    files = list(quality_folder.glob('*.png'))
    for file in files[:4]:
        print(file)
        image = cv2.imread(file, cv2.IMREAD_GRAYSCALE)
        mask = utils.segment_fingerprint(image)
        quality_scores = assess_orientation_consistency(image)
        # visualize_orientation_consistency(image, quality_scores)
        print(quality_scores)
