from pathlib import Path

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

    return imageio.v3.imread(_img_path).astype(np.float64)


def show_image_on_plot(_img, _title=None):
    """
    Function to show the image in grayscale on plot
    :param _img: Read image
    :param _title: Title of subplot of plot
    """

    plt.figure().suptitle(_title)
    plt.imshow(_img, cmap='gray')


def normalize_image(_img):
    """
    Function that normalizes image to values in range [0, 1]
    with max normalization from: https://research.ijcaonline.org/volume32/number10/pxc3875530.pdf section 4.5
    :param _img: Input fingerprint image
    :return: Normalized image with values between [0, 1]
    """
    _img = np.copy(_img)

    max_val = np.max(_img)
    if max_val > 0.0:
        _img /= max_val

    return _img


def segment_fingerprint(_img, _block_size=16, _threshold=0.3):
    """
    Function that segments foreground(fingerprint itself) and background(rest of image) of fingerprint input _img and
    returns mask.
    Function is based on ROI(Region Of Interest) where we define a region of interest based on image's blocks variance
    if variance is greater than _threshold then it is our ROI, otherwise it is the background. Then ROI is smoothed with
    morphological operations
    :param _img: Input normalized image
    :param _block_size: Size of blocks that image is divided in
    :param _threshold: Standard deviation threshold for image blocks of size _block_size
    :return: mask, that can be applied to image
    """
    (h, w) = _img.shape
    _threshold *= np.std(_img)

    img_var = np.zeros_like(_img)
    mask = np.ones_like(_img)

    for j in range(0, h, _block_size):
        for i in range(0, w, _block_size):
            # Avoid Index out of bound
            end_j = min(j + _block_size, h)
            end_i = min(i + _block_size, w)

            img_block = _img[j:end_j, i:end_i]
            block_std = np.std(img_block)
            img_var[j:end_j, i:end_i] = block_std

    mask[img_var < _threshold] = 0

    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (_block_size * 2, _block_size * 2))
    mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel)
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel)

    return mask


def display_image_and_histogram(image):
    # Create a figure with two subplots: one for the image, one for the histogram
    fig, axs = plt.subplots(1, 2, figsize=(12, 5))

    # Display the image
    axs[0].imshow(image, cmap='gray')
    axs[0].set_title('Image')
    axs[0].axis('off')

    # Display the histogram
    axs[1].hist(image.ravel(), bins=256, range=(0, 1), color='black', alpha=0.7)
    axs[1].set_title('Histogram')
    axs[1].set_xlabel('Pixel Intensity')
    axs[1].set_ylabel('Frequency')

    plt.tight_layout()
    plt.show()


def showOrientations(image, orientations, label, w=32, vmin=0.0, vmax=1.0):
    show_image_on_plot(image, label)
    height, width = image.shape
    for y in range(0, height, w):
        for x in range(0, width, w):
            if np.any(orientations[y: y + w, x: x + w] == -1.0):
                continue

            cy = (y + min(y + w, height)) // 2
            cx = (x + min(x + w, width)) // 2

            orientation = orientations[y + w // 2, x + w // 2]

            plt.plot(
                [
                    cx - w * 0.5 * np.cos(orientation),
                    cx + w * 0.5 * np.cos(orientation),
                ],
                [
                    cy - w * 0.5 * np.sin(orientation),
                    cy + w * 0.5 * np.sin(orientation),
                ],
                "r-",
                lw=1.0,
            )


def visualize_ridge_frequency(original_image, freq_image, mask):
    # Create a figure with two subplots side by side
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 6))

    # Plot original image
    ax1.imshow(original_image, cmap='gray')
    ax1.set_title('Original Image')
    ax1.axis('off')

    # Create a masked frequency image
    masked_freq = np.ma.masked_where(mask == 0, freq_image)

    # Plot frequency image
    im = ax2.imshow(masked_freq, cmap='jet', vmin=0, vmax=np.max(freq_image))
    ax2.set_title(f'Ridge Frequency')
    ax2.axis('off')

    # Add colorbar
    cbar = fig.colorbar(im, ax=ax2, orientation='vertical', shrink=0.8)
    cbar.set_label('Frequency')

    plt.tight_layout()
    plt.show()
