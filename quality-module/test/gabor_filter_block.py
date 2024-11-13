import os
import sys

import cv2
import numpy as np
from matplotlib import pyplot as plt

import utils

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

import img_orientation
import ridge_frequency


def normalize_image(_img):
    return cv2.normalize(_img.astype('float64'), None, 0.0, 1.0, cv2.NORM_MINMAX)


def segment_fingerprint(image):
    """
    Segment the foreground (fingerprint) from the background using Otsu's method and morphological operations.

    Parameters:
    image (numpy.ndarray): Input grayscale image

    Returns:
    numpy.ndarray: Binary mask with the foreground segmented
    """
    # Apply Gaussian blur to the image to reduce noise
    blurred_image = cv2.GaussianBlur(image, (5, 5), 0)

    # Apply Otsu's thresholding
    _, binary_mask = cv2.threshold(blurred_image, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

    # Invert the binary mask
    binary_mask = cv2.bitwise_not(binary_mask)

    # Apply morphological operations to remove small noise and fill gaps
    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
    binary_mask = cv2.morphologyEx(binary_mask, cv2.MORPH_CLOSE, kernel, iterations=3)
    binary_mask = cv2.morphologyEx(binary_mask, cv2.MORPH_OPEN, kernel, iterations=3)

    # Ensure there are no gaps in the mask
    binary_mask = cv2.dilate(binary_mask, kernel, iterations=3)

    binary_mask = cv2.morphologyEx(binary_mask, cv2.MORPH_CLOSE, kernel, iterations=3)

    # Convert mask to binary (0 and 1)
    binary_mask = binary_mask // 255

    return binary_mask


def apply_gabor_filter_blockwise(image, block_size, orientations, frequencies):
    h, w = image.shape
    gabor_response = np.zeros_like(image, dtype=np.float32)

    for i in range(0, h, block_size):
        for j in range(0, w, block_size):
            block = image[i:i + block_size, j:j + block_size]
            if block.shape[0] != block_size or block.shape[1] != block_size:
                continue  # Skip incomplete blocks

            print(f'Orientation size: {orientations.shape}, Frequency size: {frequencies.shape}')
            for orientation, frequency in zip(orientations, frequencies):
                    orientation = img_orientation.average_orientation(orientation)
                    frequency = ridge_frequency.average_frequencies(frequency)
                    kernel = cv2.getGaborKernel((16, 16), 4.0, orientation, frequency, 0.5, 0, ktype=cv2.CV_32F)
                    filtered = cv2.filter2D(block, cv2.CV_32F, kernel)
                    gabor_response[i:i + block_size, j:j + block_size] = np.maximum(
                        gabor_response[i:i + block_size, j:j + block_size], filtered)

    return gabor_response


# Example usage
# image = cv2.imread(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\analized_data\Poor\3.png',
#                    cv2.IMREAD_GRAYSCALE)
image = cv2.imread(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\analized_data\Excellent\5.png',
                   cv2.IMREAD_GRAYSCALE)
mask = segment_fingerprint(image)
image = normalize_image(image)
block_size = 32
orientations, _ = img_orientation.estimate_orientation(image, _interpolate=True)
orientations = (orientations + (np.pi / 2)) % np.pi
orientations = np.where(mask == 1.0, orientations, -1.0)
frequencies = ridge_frequency.estimate_frequencies(image, orientations, block_size)
frequencies = np.where(mask == 1.0, frequencies, -1.0)

gabor_response = apply_gabor_filter_blockwise(image, block_size, orientations, frequencies)
plt.imshow(utils.normalize(gabor_response), cmap='gray')
plt.show()
