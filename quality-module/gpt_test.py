from pathlib import Path

import cv2
from matplotlib import pyplot as plt

import numpy as np
from scipy import ndimage, signal
from skimage.filters import gabor_kernel
from scipy.fft import fft2, fftshift


def estimate_local_orientation(image, block_size=16, sigma=5):
    """
    Estimate local orientation field using gradient-based method.

    Args:
        image: Grayscale fingerprint image
        block_size: Size of the block for orientation estimation
        sigma: Standard deviation for Gaussian smoothing

    Returns:
        orientation: Orientation field
        coherence: Coherence field (reliability measure)
    """
    # Calculate gradients
    gy, gx = np.gradient(image)

    # Calculate gradient squares
    gxx = gx * gx
    gyy = gy * gy
    gxy = gx * gy

    # Apply block averaging
    kernel_size = block_size
    kernel = np.ones((kernel_size, kernel_size))

    gxx_block = ndimage.convolve(gxx, kernel)
    gyy_block = ndimage.convolve(gyy, kernel)
    gxy_block = ndimage.convolve(gxy, kernel)

    # Calculate orientation
    orientation = 0.5 * np.arctan2(2 * gxy_block, gxx_block - gyy_block)

    # Calculate coherence
    denom = np.sqrt((gxx_block - gyy_block) ** 2 + 4 * gxy_block ** 2)
    numer = np.sqrt(gxx_block + gyy_block)
    coherence = np.divide(denom, 2 * numer, out=np.zeros_like(denom), where=numer != 0)

    return orientation, coherence


def estimate_ridge_frequency(image, orientation, block_size=16, window_size=64):
    """
    Estimate ridge frequency in local blocks.

    Args:
        image: Grayscale fingerprint image
        orientation: Local orientation field
        block_size: Size of blocks for frequency estimation
        window_size: Size of window for frequency analysis

    Returns:
        frequency: Ridge frequency field
    """
    rows, cols = image.shape
    frequency = np.zeros((rows // block_size, cols // block_size))

    for i in range(0, rows - block_size, block_size):
        for j in range(0, cols - block_size, block_size):
            block = image[i:i + window_size, j:j + window_size]
            angle = orientation[i // block_size, j // block_size]

            # Rotate block to align ridges vertically
            rotated = ndimage.rotate(block, angle * 180 / np.pi - 90,
                                     reshape=False, order=3)

            # Get projection
            projection = np.sum(rotated, axis=1)

            # Find peaks
            peaks, _ = signal.find_peaks(projection)

            if len(peaks) >= 2:
                # Calculate average distance between peaks
                peak_distances = np.diff(peaks)
                if len(peak_distances) > 0:
                    frequency[i // block_size, j // block_size] = 1.0 / np.mean(peak_distances)

    return frequency


def apply_gabor_filter(image, orientation, frequency, block_size=16):
    """
    Apply Gabor filtering for enhancement and quality assessment.

    Args:
        image: Grayscale fingerprint image
        orientation: Local orientation field
        frequency: Ridge frequency field
        block_size: Size of blocks

    Returns:
        enhanced: Enhanced image
        response: Gabor filter response (quality measure)
    """
    rows, cols = image.shape
    enhanced = np.zeros_like(image, dtype=np.float64)
    response = np.zeros((rows // block_size, cols // block_size))

    # Default frequency if estimation fails
    default_freq = 1 / 9.0  # Assuming average ridge period of 9 pixels

    for i in range(0, rows - block_size, block_size):
        for j in range(0, cols - block_size, block_size):
            freq = frequency[i // block_size, j // block_size]
            if freq == 0:
                freq = default_freq

            angle = orientation[i // block_size, j // block_size]

            # Create Gabor kernel
            kernel = gabor_kernel(freq, theta=angle, sigma_x=4.0, sigma_y=4.0)

            # Apply filter
            block = image[i:i + block_size, j:j + block_size]
            filtered = signal.convolve2d(block, np.real(kernel), mode='same')
            enhanced[i:i + block_size, j:j + block_size] = filtered

            # Calculate response as measure of quality
            response[i // block_size, j // block_size] = np.mean(np.abs(filtered))

    return enhanced, response


def calculate_snr(image, block_size=16):
    """
    Calculate Signal-to-Noise Ratio in local blocks.

    Args:
        image: Grayscale fingerprint image
        block_size: Size of blocks

    Returns:
        snr: Local SNR values
    """
    rows, cols = image.shape
    snr = np.zeros((rows // block_size, cols // block_size))

    for i in range(0, rows - block_size, block_size):
        for j in range(0, cols - block_size, block_size):
            block = image[i:i + block_size, j:j + block_size]

            # Estimate signal power (variance of block)
            signal_power = np.var(block)

            # Estimate noise power (high-frequency components)
            freq_block = fft2(block)
            freq_block = fftshift(freq_block)

            # Create high-pass mask
            center_row, center_col = block_size // 2, block_size // 2
            Y, X = np.ogrid[-center_row:block_size - center_row, -center_col:block_size - center_col]
            dist_from_center = np.sqrt(X * X + Y * Y)
            mask = dist_from_center > block_size // 4

            # Calculate noise power from high-frequency components
            noise_power = np.sum(np.abs(freq_block * mask)) / np.sum(mask)

            if noise_power > 0:
                snr[i // block_size, j // block_size] = 10 * np.log10(signal_power / noise_power)

    return snr


def calculate_quality_score(coherence, frequency, gabor_response, snr,
                            w1=0.3, w2=0.2, w3=0.3, w4=0.2):
    """
    Calculate overall quality score combining multiple measures.

    Args:
        coherence: Coherence field
        frequency: Ridge frequency field
        gabor_response: Gabor filter response
        snr: Signal-to-noise ratio
        w1, w2, w3, w4: Weights for different measures

    Returns:
        quality_score: Overall quality score between 0 and 1
    """
    # Normalize all measures to [0, 1]
    coherence_norm = (coherence - np.min(coherence)) / (np.max(coherence) - np.min(coherence) + 1e-10)
    frequency_norm = (frequency - np.min(frequency)) / (np.max(frequency) - np.min(frequency) + 1e-10)
    gabor_norm = (gabor_response - np.min(gabor_response)) / (np.max(gabor_response) - np.min(gabor_response) + 1e-10)
    snr_norm = (snr - np.min(snr)) / (np.max(snr) - np.min(snr) + 1e-10)

    # Combine measures using weighted sum
    quality_score = (w1 * coherence_norm + w2 * frequency_norm +
                     w3 * gabor_norm + w4 * snr_norm)

    return quality_score


def estimate_fingerprint_quality(image, block_size=16):
    """
    Main function to estimate fingerprint quality.

    Args:
        image: Grayscale fingerprint image
        block_size: Size of blocks for local analysis

    Returns:
        quality_map: Local quality scores
        global_quality: Global quality score
    """
    # Calculate local orientation and coherence
    orientation, coherence = estimate_local_orientation(image, block_size)

    # Estimate ridge frequency
    frequency = estimate_ridge_frequency(image, orientation, block_size)

    # Apply Gabor filtering
    enhanced, gabor_response = apply_gabor_filter(image, orientation, frequency, block_size)

    # Calculate SNR
    snr = calculate_snr(image, block_size)

    # Calculate quality scores
    quality_map = calculate_quality_score(coherence, frequency, gabor_response, snr)

    # Calculate global quality (weighted by distance from center)
    rows, cols = quality_map.shape
    center_y, center_x = rows // 2, cols // 2
    Y, X = np.ogrid[:rows, :cols]
    dist_from_center = np.sqrt((X - center_x) ** 2 + (Y - center_y) ** 2)
    weights = np.exp(-dist_from_center / (max(rows, cols) / 4))

    global_quality = np.sum(quality_map * weights) / np.sum(weights)

    return quality_map, global_quality



folder = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\analized_data')
quality_folders = [f for f in folder.iterdir() if f.is_dir()]

for quality_folder in quality_folders:
    quality_class = quality_folder.name
    files = list(quality_folder.glob('*.png'))

    for file in files[:2]:
        image = cv2.imread(file, cv2.IMREAD_GRAYSCALE)
        map, global_q = estimate_fingerprint_quality(image)

        plt.imshow(image, cmap='jet')
        plt.show()
        plt.imshow(global_q, cmap='jet')
        plt.show()
