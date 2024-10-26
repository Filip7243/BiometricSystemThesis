from pathlib import Path

import numpy as np
import cv2
from scipy.ndimage import binary_closing


def fft_mask_segmentation(img_block, low_freq_threshold=5, high_freq_threshold=50):
    """
    Segment foreground from background in a fingerprint image block using FFT.

    Parameters:
    - img_block: Grayscale image block (2D numpy array) to segment.
    - low_freq_threshold: Lower frequency bound to keep (default: 5).
    - high_freq_threshold: Upper frequency bound to keep (default: 50).

    Returns:
    - mask: Binary mask where the foreground is 1 and background is 0.
    """
    # Step 1: Apply FFT to the image block
    fft = np.fft.fft2(img_block)
    fft_shifted = np.fft.fftshift(fft)  # Shift FFT for easier frequency filtering

    # Step 2: Create a frequency filter (band-pass filter)
    rows, cols = img_block.shape
    crow, ccol = rows // 2, cols // 2  # Center of the FFT image
    mask = np.zeros((rows, cols), dtype=np.float32)

    for i in range(rows):
        for j in range(cols):
            # Distance from the center (crow, ccol) in frequency space
            d = np.sqrt((i - crow) ** 2 + (j - ccol) ** 2)

            # Keep frequencies in the range [low_freq_threshold, high_freq_threshold]
            if low_freq_threshold < d < high_freq_threshold:
                mask[i, j] = 1  # Pass these frequencies

    # Step 3: Apply the filter to the FFT
    filtered_fft = fft_shifted * mask

    # Step 4: Inverse FFT to return to the spatial domain
    filtered_img = np.fft.ifft2(np.fft.ifftshift(filtered_fft))
    filtered_img = np.abs(filtered_img)

    # Step 5: Threshold the filtered image to create a binary mask
    _, binary_mask = cv2.threshold(filtered_img, np.mean(filtered_img), 255, cv2.THRESH_BINARY)
    binary_mask = binary_mask.astype(np.uint8)

    # Optional: Morphological operations to clean up the mask
    binary_mask = binary_closing(binary_mask, structure=np.ones((3, 3)))

    return binary_mask


def normalize_image(img, target_mean=128, target_std=50):
    # Calculate the mean and standard deviation of the image
    img_mean = np.mean(img)
    img_std = np.std(img)

    # Apply normalization
    normalized_img = (img - img_mean) / (img_std + 1e-5)  # Prevent division by zero
    normalized_img = normalized_img * target_std + target_mean

    # Clip values to ensure they remain in the valid grayscale range
    normalized_img = np.clip(normalized_img, 0, 255).astype(np.uint8)
    return normalized_img


def otsu_fingerprint_mask(img):
    """
    Create a binary mask of the fingerprint area using Otsu's thresholding.
    In the mask, the fingerprint area will be white (255) and the background black (0).

    Parameters:
    - img: Grayscale fingerprint image (2D numpy array).

    Returns:
    - mask: Binary mask where fingerprint area is white and background is black.
    """

    # Step 1: Apply Gaussian Blur to reduce noise (optional, but improves Otsu performance)
    blurred_img = cv2.GaussianBlur(img, (5, 5), 0)

    # Step 2: Otsu’s thresholding to separate foreground and background
    _, otsu_thresh = cv2.threshold(blurred_img, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

    # Step 3: Invert the mask if needed, so ridges are white (foreground)
    mask = cv2.bitwise_not(otsu_thresh)

    # Step 4: Optional - Apply morphological closing to remove small holes inside ridges
    # kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (11, 11))
    # closed_img = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel)
    #
    # segmented_img = np.where(closed_img == 255, 255, 0).astype(np.uint8)

    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (11, 11))
    dilated_img = cv2.dilate(mask, kernel, iterations=2)
    closed_img = cv2.morphologyEx(dilated_img, cv2.MORPH_CLOSE, kernel, iterations=2)
    segmented_img = np.where(closed_img == 255, 255, 0).astype(np.uint8)

    return segmented_img


folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB')
# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB\images\500\png\plain')
tif_files = list(folder.glob('*.tif'))

for img_path in tif_files:
    img = cv2.imread(str(img_path), cv2.IMREAD_GRAYSCALE)
    img = normalize_image(img)
    mask = otsu_fingerprint_mask(img)

    print(f'mask: {np.min(mask):.2f}, {np.max(mask):.2f}')

    img2 = np.where(mask == 255, 0, img)

    cv2.imshow('img', img)
    cv2.imshow('img2', img2)
    cv2.imshow('mask', mask)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
# # Example usage:
# img = cv2.imread('fingerprint_image.png', cv2.IMREAD_GRAYSCALE)
# block_size = 32  # Example block size
#
# # Extract a block from the image (assume img is large)
# img_block = img[0:block_size, 0:block_size]
#
# # Generate a mask for this block using the FFT-based segmentation
# mask = fft_mask_segmentation(img_block)
#
# # Display results
# cv2.imshow('Original Block', img_block)
# cv2.imshow('Segmentation Mask', mask)
# cv2.waitKey(0)
# cv2.destroyAllWindows()
