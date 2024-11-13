import cv2
import imageio
import numpy as np
from matplotlib import pyplot as plt


def read_image(_img_path):
    """
    Function to read the image as numpy array of floats
    :param _img_path: Path of input fingerprint image
    :return: numpy array of floats
    """

    return cv2.imread(_img_path, cv2.IMREAD_GRAYSCALE)


def show_image_on_plot(_img, _title=None):
    """
    Function to show the image in grayscale on plot
    :param _img: Read image
    :param _title: Title of subplot of plot
    """

    plt.figure().suptitle(_title)
    plt.imshow(_img, cmap='gray')


def normalize(data):
    """
    Function that normalizes image to values in range [0, 1]
    with max normalization from: https://research.ijcaonline.org/volume32/number10/pxc3875530.pdf section 4.5
    :param data: data like orientations etc.
    :return: Normalized image with values between [0, 1]
    """

    data = np.copy(data)
    data -= np.min(data)

    max_val = np.max(data)
    if max_val > 0.0:
        data /= max_val

    return data


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


def showOrientations(_img, _orientations, _label, _block_size=32):
    show_image_on_plot(_img, _label)
    height, width = _img.shape
    for y in range(0, height, _block_size):
        for x in range(0, width, _block_size):
            if np.any(_orientations[y: y + _block_size, x: x + _block_size] == -1.0):
                continue

            cy = (y + min(y + _block_size, height)) // 2
            cx = (x + min(x + _block_size, width)) // 2

            orientation = _orientations[y + _block_size // 2, x + _block_size // 2]

            plt.plot(
                [
                    cx - _block_size * 0.5 * np.cos(orientation),
                    cx + _block_size * 0.5 * np.cos(orientation),
                ],
                [
                    cy - _block_size * 0.5 * np.sin(orientation),
                    cy + _block_size * 0.5 * np.sin(orientation),
                ],
                "r-",
                lw=1.0,
            )
