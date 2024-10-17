from pathlib import Path

import cv2
import imageio
import numpy as np
from matplotlib import pyplot as plt

import img_orientation
import ridge_frequency


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


folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB')
# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB\images\500\png\plain')
tif_files = list(folder.glob('*.tif'))


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


for tif_file in tif_files:
    img = read_image(tif_file)
    show_image_on_plot(img, tif_file)

    norm_img = normalize_image(img)
    # show_image_on_plot(norm_img, tif_file)

    mask = segment_fingerprint(norm_img)
    # mask2 = img_segmentation.create_fingerprint_mask(img, k=16)  # TODO: imporve it, it works too long
    show_image_on_plot(mask, tif_file)
    # show_image_on_plot(mask2, tif_file)

    est_orientation, coh = img_orientation.estimate_orientation(norm_img, _interpolate=True)

    consistency = img_orientation.measure_orientation_consistency(norm_img, est_orientation, _block_size=16)
    where = np.where(mask == 1.0, consistency, -1.0)
    result = np.clip(where, 0, 1)

    # frequencies, median_frequency = ridge_frequency.estimate_ridge_frequency(_img=norm_img, _mask=mask,
    #                                                                          _orientations=est_orientation,
    #                                                                          _block_size=32,
    #                                                                          _window_size=5,
    #                                                                          _min_wave_len=5, _max_wave_len=15)

    frequencies = np.where(mask == 1.0, ridge_frequency.estimate_frequencies(norm_img, est_orientation), -1.0)
    show_image_on_plot(normalize_image(frequencies), "frequencies")

    non_zero_freq = frequencies[frequencies > 0]
    print(f"Min frequency: {non_zero_freq.min()}")
    print(f"Max frequency: {non_zero_freq.max()}")
    print(f"Mean frequency: {non_zero_freq.mean()}")
    print(f"Median frequency: {np.median(non_zero_freq)}")


    # where = where[where >= 0]
    # mean_consistency = np.mean(where)
    # print(f'mean_consistency: {mean_consistency}')
    # high_consistency_count = np.sum(where > 0.5)
    # print(f'high_consistency_count: {high_consistency_count}')
    # total_pixels = where.size
    # print(f'total_pixels: {total_pixels}')
    # proportion_high_consistency = high_consistency_count / total_pixels
    # print(f'proportion_high_consistency: {proportion_high_consistency}')
    # quality_score = (mean_consistency + proportion_high_consistency) / 2
    # print(f'quality_score: {quality_score}')

    # consistency_map = plt.imshow(result, cmap='jet')
    # plt.colorbar(consistency_map)
    # plt.title('Orientation Consistency')
    # plt.axis('off')
    # plt.show()

    # orientations = np.where(mask == 1.0, est_orientation, -1.0)

    # cohs = np.where(mask == 1.0, coh, -1.0)

    # valid_coherence = cohs[cohs >= 0]
    # good_regions = np.sum(valid_coherence > 0.65) / len(valid_coherence)
    # mean_coherence = np.mean(valid_coherence)
    # weighted_score = np.sum(valid_coherence ** 2) / len(valid_coherence)
    # combined_score = 0.4 * good_regions + 0.3 * mean_coherence + 0.3 * weighted_score

    # plt.imshow(cohs, cmap='hot')
    # plt.colorbar(label='Coherence')
    # plt.title('Coherence Map')
    # plt.show()
    #
    # showOrientations(img, orientations, "orientations", 8)

    # TODO: estimate frequncie, segmentaion mask, coherence, consistency

    plt.show()
