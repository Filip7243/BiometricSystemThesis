import numpy as np
import matplotlib.pyplot as plt
from scipy import ndimage

import gabor_filter as gb
import img_orientation
import ridge_frequency
import utils


def compute_snr(img, mask):
    """
    Compute the signal-to-noise ratio (SNR) for a fingerprint image.

    Parameters:
    img (numpy.ndarray): The normalized fingerprint image.
    mask (numpy.ndarray): The segmentation mask for the fingerprint region.

    Returns:
    float: The signal-to-noise ratio.
    """
    # Extract the fingerprint region
    fingerprint = img * mask

    # Compute the signal power
    signal_power = np.var(fingerprint[mask == 1])

    # Compute the noise power
    noise_power = np.var(img[mask == 0])

    # Calculate the SNR
    if noise_power == 0:
        return 0
    snr = 10 * np.log10(signal_power / noise_power)

    return snr


def compute_gabor_snr(image, mask, block_size=16):
    """
    Fast and efficient SNR computation using Gabor filter responses.

    Args:
        image: Input fingerprint image (grayscale)
        orientations: Orientation field
        frequencies: Ridge frequency field
        block_size: Size of blocks for analysis

    Returns:
        tuple: (snr_map, overall_snr)
    """
    height, width = image.shape
    blocks_y = height // block_size
    blocks_x = width // block_size

    # Initialize SNR map
    snr_map = np.zeros((blocks_y, blocks_x))

    orientations, _ = np.where(mask == 1.0, img_orientation.estimate_orientation(image, _interpolate=True), -1.0)
    frequencies = np.where(mask == 1.0, ridge_frequency.estimate_frequencies(image, orientations, block_size), -1.0)
    filtered_image = np.zeros_like(image)

    # Process each block
    for i in range(blocks_y):
        for j in range(blocks_x):
            # Block coordinates
            y_start = i * block_size
            x_start = j * block_size
            y_end = y_start + block_size
            x_end = x_start + block_size

            # Extract block data
            block = image[y_start:y_end, x_start:x_end]
            block_mask = mask[y_start:y_end, x_start:x_end]
            if np.sum(block_mask) > (block_size * block_size) * 0.5:
                block_orientation = img_orientation.average_orientation(orientations[y_start:y_end, x_start:x_end])
                block_frequency = ridge_frequency.average_frequencies(frequencies[y_start:y_end, x_start:x_end])

                # Skip invalid frequency blocks
                if block_frequency <= 0:
                    continue

                # Create and apply Gabor filter
                gabor_kernel = gb.create_gabor_filter(
                    block_size,
                    block_orientation,
                    block_frequency,
                    x_sigma=4,
                    y_sigma=4
                )

                filtered_block = ndimage.convolve(block, gabor_kernel, mode='reflect')
                filtered_image[y_start:y_end, x_start:x_end] = filtered_block

                # Calculate SNR
                signal_power = np.mean(filtered_block ** 2)
                noise = abs(block - filtered_block)
                noise_power = np.mean(noise ** 2) + 1e-10

                if noise_power > 1e-10:  # Avoid division by zero
                    snr_map[i, j] = 10 * np.log10(signal_power / noise_power)

    # Calculate overall SNR (excluding zeros)
    valid_snr = snr_map[snr_map != 0]
    overall_snr = np.mean(valid_snr) if len(valid_snr) > 0 else 0

    plt.imshow(filtered_image, cmap='gray')
    print(np.mean(filtered_image[mask == 1.0]), np.std(filtered_image[mask == 1.0]))

    return snr_map, overall_snr


def classify_fingerprint_quality(snr):
    """
    Classify fingerprint quality based on SNR value.

    Thresholds based on empirical analysis:
    - Excellent: SNR > 15 dB
    - Good: 10 dB < SNR ≤ 15 dB
    - Fair: 5 dB < SNR ≤ 10 dB
    - Poor: 0 dB < SNR ≤ 5 dB
    - Very Poor: SNR ≤ 0 dB
    """
    if snr > 15:
        return "Excellent"
    elif snr > 10:
        return "Good"
    elif snr > 5:
        return "Fair"
    elif snr > 0:
        return "Poor"
    else:
        return "Very Poor"


def plot_snr_results(image, snr_map, overall_snr):
    """
    Create simple visualization of SNR results.
    """
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(10, 4))

    # Original image
    ax1.imshow(image, cmap='gray')
    ax1.set_title('Original Image')
    ax1.axis('off')

    # SNR map
    im = ax2.imshow(snr_map, cmap='jet', vmin=-20, vmax=40)
    ax2.set_title(f'SNR Map\nOverall SNR: {overall_snr:.1f} dB\n'
                  f'Quality: {classify_fingerprint_quality(overall_snr)}')
    ax2.axis('off')

    # Add colorbar
    plt.colorbar(im, ax=ax2, label='SNR (dB)')

    plt.tight_layout()
    return fig


# Example usage
def process_fingerprint(image, mask):
    """
    Process fingerprint image and display results.
    """
    # Compute SNR
    snr_map, overall_snr = compute_gabor_snr(image, mask)

    # Plot results
    fig = plot_snr_results(image, snr_map, overall_snr)
    plt.show()

    # Print classification
    quality_class = classify_fingerprint_quality(overall_snr)
    print(f"\nFingerprint Quality Assessment:")
    print(f"Overall SNR: {overall_snr:.1f} dB")
    print(f"Quality Class: {quality_class}")
