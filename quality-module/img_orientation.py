import cv2
import numpy as np
from scipy import ndimage
from scipy.stats import norm


def compute_snr(image: np.ndarray, filtered_image: np.ndarray, mask: np.ndarray) -> float:
    """
    Compute the Signal-to-Noise Ratio (SNR) of the fingerprint image.

    Parameters:
    image (numpy.ndarray): The original fingerprint image.
    filtered_image (numpy.ndarray): The filtered fingerprint image.
    mask (numpy.ndarray): The mask indicating the fingerprint region.

    Returns:
    float: The SNR value.
    """
    signal = image[mask == 1.0]
    noise = (image[mask == 1.0] - filtered_image[mask == 1.0])
    signal_power = np.mean(signal ** 2)
    noise_power = np.mean(noise ** 2)

    if noise_power == 0:
        return float('inf')  # Avoid division by zero

    snr = 10 * np.log10(signal_power / noise_power)
    return snr


def compute_cnr(image: np.ndarray, mask: np.ndarray) -> float:
    """
    Compute the Contrast-to-Noise Ratio (CNR) of the fingerprint image.

    Parameters:
    image (numpy.ndarray): The original fingerprint image.
    mask (numpy.ndarray): The mask indicating the fingerprint region.

    Returns:
    float: The CNR value.
    """
    foreground = image[mask == 1.0]
    background = image[mask == 0.0]
    contrast = np.abs(np.mean(foreground) - np.mean(background))
    noise = np.std(background)
    cnr = contrast / noise
    return cnr


def compute_metric_error_pdf(metric_map):
    """
    Compute the probability density function (PDF) of errors within the metric map.

    Parameters:
    metric_map (numpy.ndarray): The metric map (e.g., ridge frequency or Gabor filter response).

    Returns:
    float: The error PDF score.
    """
    block_size = 16
    metric_errors = []

    for y in range(0, metric_map.shape[0], block_size):
        for x in range(0, metric_map.shape[1], block_size):
            block_metric = metric_map[y:y + block_size, x:x + block_size]
            block_mean = np.mean(block_metric)
            block_errors = np.abs(block_metric - block_mean)
            metric_errors.extend(block_errors.ravel())

    metric_errors = np.array(metric_errors)
    metric_error_pdf = norm.pdf(metric_errors, loc=0, scale=np.std(metric_errors)).mean()
    return metric_error_pdf


def clarity_and_strength(_img, _mask, _block_size=16):
    (h, w) = _img.shape
    y_blocks, x_blocks = h // _block_size, w // _block_size

    clarity_scores = []
    freq_strengths = []
    for j in range(y_blocks):
        for i in range(x_blocks):
            y_start, y_end = j * _block_size, (j + 1) * _block_size
            x_start, x_end = i * _block_size, (i + 1) * _block_size
            block_image = _img[y_start:y_end, x_start:x_end]
            block_mask = _mask[y_start:y_end, x_start:x_end]

            if np.all(block_mask == 1.0):
                clarity_scores.append(np.std(block_image))

                fft = np.fft.fft2(block_image)
                fft_mag = np.abs(np.fft.fftshift(fft))
                freq_strength = np.max(fft_mag) / np.mean(fft_mag)
                freq_strength = np.clip(freq_strength / 100.0, 0, 1)  # Normalize and clip
                freq_strengths.append(freq_strength)

    return np.mean(clarity_scores), np.std(clarity_scores), freq_strengths


def estimate_orientation(_img, _block_size=16, _interpolate=False):
    """
    Function to estimate the orientation field of each block of size _block_size in _img.
    Idea and algorithm to estimate orientations for each block is from:
    https://biometrics.cse.msu.edu/Publications/Fingerprint/MSU-CPS-97-35fenhance.pdf - section 2.4
    Idea of applying gaussian filter before estimating orientations is from:
    https://www.researchgate.net/publication/225820611_Fingerprint_Orientation_Field_Enhancement - section 3
    and I also optimized and refactor part of code from here (interpolation part):
    :param _img: Input image, should be normalized
    :param _block_size: size of block that image will be divided by
    :param _interpolate: boolean flag whether to interpolate orientations
    :return: nparray with estimated orientations of input image
    """

    (h, w) = _img.shape

    # Smooth input image
    _img = ndimage.filters.gaussian_filter(_img, 2.0)

    # Create gradients based on sobel kernels
    gx, gy = (cv2.Sobel(_img, cv2.CV_64F, 1, 0, ksize=3),
              cv2.Sobel(_img, cv2.CV_64F, 0, 1, ksize=3))

    y_blocks, x_blocks = h // _block_size, w // _block_size

    theta = np.empty((y_blocks, x_blocks))  # here will be stored estimated orientation of each block
    coherence = np.empty((y_blocks, x_blocks))

    for j in range(y_blocks):
        for i in range(x_blocks):
            y_start, y_end = j * _block_size, (j + 1) * _block_size
            x_start, x_end = i * _block_size, (i + 1) * _block_size

            gx_block = gx[y_start: y_end, x_start: x_end]
            gy_block = gy[y_start: y_end, x_start: x_end]

            root_gradient_x = gx_block ** 2
            root_gradient_y = gy_block ** 2

            g_xx = np.sum(root_gradient_x)
            g_yy = np.sum(root_gradient_y)
            g_xy = np.sum(gx_block * gy_block)

            v_x = 2 * g_xy
            v_y = np.sum(root_gradient_x - root_gradient_y)

            theta[j, i] = np.arctan2(v_x, v_y) * 0.5

            numerator = np.sqrt((g_xx - g_yy) ** 2 + 4 * (g_xy ** 2))
            denominator = g_xx + g_yy

            coherence[j, i] = 0 if denominator == 0 else numerator / denominator

    # Adjust theta by adding 90 degrees (pi/2) and take modulo pi to ensure it stays within the range [0, pi)
    theta = (theta + np.pi * 0.5) % np.pi

    # Averaging angels based on their neighbours
    theta_averaged = np.empty_like(theta)
    theta = np.pad(theta, 2, mode='edge')
    for j in range(y_blocks):
        for i in range(x_blocks):
            neighbours_theta = theta[j: j + 5, i: i + 5]
            avg_neighbours, std = average_orientation(neighbours_theta, _std=True)
            if std > 0.3:  # It suggests big noise between angels  #TODO: try another threshold for std (lower = better)
                avg_neighbours = theta[j + 2, i + 2]  # Take center block
            theta_averaged[j, i] = avg_neighbours

    theta = theta_averaged

    # Interpolation (or/and back to original shape)
    orientations = np.full(_img.shape, -1.0)
    coherences = np.full(_img.shape, -1.0)
    if _interpolate:
        orientations, coherences = interpolate(_img.shape, _block_size, theta, coherence)
    else:
        for j in range(y_blocks):
            for i in range(x_blocks):
                j_start = j * _block_size
                j_end = (j + 1) * _block_size
                i_start = i * _block_size
                i_end = (i + 1) * _block_size
                orientations[j_start:j_end, i_start:i_end] = theta[j, i]
                coherences[j_start:j_end, i_start:i_end] = coherences[j, i]

    return orientations, coherences


def average_orientation(_orientations, _std=False):
    """
    Function that aligns orientations by averaging them in range to 90deg max
    :param _orientations: orientations to align(average)
    :param _std: boolean flag whether to use standard deviation in return
    :return: aligned input orientations
    """

    _orientations = np.asarray(_orientations).flatten()  # 2D -> 1D
    angle_reference = _orientations[0]  # Based on this angle, other will be aligned

    aligned = np.where(
        # Check if (orientation - reference) is greater than 90deg
        np.absolute(_orientations - angle_reference) > np.pi * 0.5,
        # If orientation greater than reference then sub 180deg (pi), else add 180deg
        np.where(_orientations > angle_reference, _orientations - np.pi, _orientations + np.pi),
        _orientations
    )

    if _std:
        return np.average(aligned) % np.pi, np.std(aligned)
    else:
        return np.average(aligned) % np.pi


def interpolate(_img_shape, _block_size, _theta, _coherence):
    """
    Function that uses bilinear interpolation,
    papers: https://www.researchgate.net/publication/366816309_Performance_Analysis_on_Interpolation-based_Methods_for_Fingerprint_Images,
    https://biometrics.cse.msu.edu/Publications/Fingerprint/MSU-CPS-97-35fenhance.pdf
    Based on first paper it is not the best interpolation type in context of fingerprint image processing,
    but not the worst too.
    Second paper gave me just the idea of interpolation based on neighbouring blocks (Section 2.5, point 5)
    :param _img_shape: Shape of image
    :param _block_size: Size of block that image is divided of
    :param _theta: nparray with orientations of each block
    :param _coherence: coherence values
    :return: Interpolated orientations
    """

    orientations = np.full(_img_shape, -1.0)  # Result array
    coherences = np.full(_img_shape, -1.0)  # Result array

    y_blocks, x_blocks = _img_shape[0] // _block_size, _img_shape[1] // _block_size

    half_block_size = _block_size // 2

    iy, ix = np.meshgrid(np.arange(_block_size), np.arange(_block_size), indexing='ij')

    # Bilinear Interpolation weights
    weights = np.array([
        (_block_size - iy) * (_block_size - ix),  # top-left
        iy * (_block_size - ix),  # bottom-left
        (_block_size - iy) * ix,  # top-right
        iy * ix  # bottom-right
    ])
    weights = weights / weights.sum(axis=0)  # normalized to [0, 1]

    for j in range(y_blocks - 1):
        for i in range(x_blocks - 1):
            orientations_neighbours = np.array([
                _theta[j, i],  # top-left
                _theta[j + 1, i],  # bottom-left
                _theta[j, i + 1],  # top-right
                _theta[j + 1, i + 1]  # bottom-right
            ])

            coherences_neighbours = np.array([
                _coherence[j, i],  # top-left
                _coherence[j + 1, i],  # bottom-left
                _coherence[j, i + 1],  # top-right
                _coherence[j + 1, i + 1]  # bottom-right
            ])

            complex_orientations = np.exp(2j * orientations_neighbours)
            interpolated_complex = np.sum(weights * complex_orientations[:, np.newaxis, np.newaxis], axis=0)
            interpolated_angles = np.angle(interpolated_complex) / 2

            interpolated_coherences = np.sum(weights * coherences_neighbours[:, np.newaxis, np.newaxis], axis=0)

            j_slice = slice(j * _block_size + half_block_size, j * _block_size + half_block_size + _block_size)
            i_slice = slice(i * _block_size + half_block_size, i * _block_size + half_block_size + _block_size)

            orientations[j_slice, i_slice] = interpolated_angles
            coherences[j_slice, i_slice] = interpolated_coherences

    return orientations, coherences
