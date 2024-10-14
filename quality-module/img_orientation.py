import numpy as np
from scipy import ndimage
from scipy.ndimage.filters import convolve


def get_sobel_x():
    return np.array([
        [-1, 0, 1],
        [-2, 0, 2],
        [-1, 0, 1]
    ])


def get_sobel_y():
    return np.array([
        [-1, -2, -1],
        [0, 0, 0],
        [1, 2, 1]
    ])


def estimate_orientation(_img, _block_size=16, _interpolate=False):
    """
    Function to estimate the orientation field of each block of size _block_size in _img.
    Idea and algorithm to estimate orientations for each block is from:
    https://biometrics.cse.msu.edu/Publications/Fingerprint/MSU-CPS-97-35fenhance.pdf - section 2.4
    :param _img: Input image, should be normalized
    :param _block_size: size of block that image will be divided by
    :param _interpolate: boolean flag whether to interpolate orientations
    :return: nparray with estimated orientations of input image
    """

    (h, w) = _img.shape

    # Smooth input image
    _img = ndimage.filters.gaussian_filter(_img, 2.0)

    # Create gradients based on sobel kernels
    gradient_x, gradient_y = convolve(_img, get_sobel_x()), convolve(_img, get_sobel_y())

    y_blocks, x_blocks = h // _block_size, w // _block_size

    theta = np.empty((y_blocks, x_blocks))  # here will be stored estimated orientation of each block
    coherence = np.empty((y_blocks, x_blocks))

    for j in range(y_blocks):
        for i in range(x_blocks):
            V_y, V_x = 0.0, 0.0
            G_x_sum, G_y_sum = 0.0, 0.0
            G_x_y_sum = 0.0

            for v in range(_block_size):
                for u in range(_block_size):
                    y_slice = j * _block_size + v
                    x_slice = i * _block_size + u

                    G_x = gradient_x[y_slice, x_slice]
                    G_y = gradient_y[y_slice, x_slice]

                    V_x += 2 * G_x * G_y
                    V_y += (G_x ** 2 - G_y ** 2)

                    G_x_sum += G_x ** 2
                    G_y_sum += G_y ** 2
                    G_x_y_sum += G_x * G_y

            theta[j, i] = np.arctan2(V_x, V_y) * 0.5

            numerator = np.sqrt((G_x_sum - G_y_sum) ** 2 + 4 * (G_x_y_sum ** 2))
            denominator = G_x_sum + G_y_sum

            if denominator == 0:
                coherence[j, i] = 0
            else:
                coherence[j, i] = numerator / denominator

    # Adjust theta by adding 90 degrees (pi/2) and take modulo pi to ensure it stays within the range [0, pi)
    theta = (theta + np.pi * 0.5) % np.pi

    # TODO: maybe Gaussian Blur will be sufficient

    # Averaging angels based on their neighbours
    theta_averaged = np.empty_like(theta)
    theta = np.pad(theta, 2, mode='edge')
    for j in range(y_blocks):
        for i in range(x_blocks):
            neighbours = theta[j: j + 5, i: i + 5]
            avg_neighbours, std = average_orientation(neighbours, _std=True)
            if std > 0.5:  # It suggests big noise between angels
                avg_neighbours = theta[j + 2, i + 2]  # Take center block
            theta_averaged[j, i] = avg_neighbours

    theta = theta_averaged

    # Interpolation (back to original shape)
    orientations = np.full(_img.shape, -1.0)
    if _interpolate:
        orientations = bilinear_interpolation(_img.shape, _block_size, theta)
    else:
        for j in range(y_blocks):
            for i in range(x_blocks):
                j_start = j * _block_size
                j_end = (j + 1) * _block_size
                i_start = i * _block_size
                i_end = (i + 1) * _block_size
                orientations[j_start:j_end, i_start:i_end] = theta[j, i]

    return orientations, coherence


def average_orientation(_orientations, _weights=None, _std=False):
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
        return np.average(aligned, weights=_weights) % np.pi, np.std(aligned)
    else:
        return np.average(aligned, weights=_weights) % np.pi


def bilinear_interpolation(_img_shape, _block_size, theta):
    """
    Function that uses bilinear interpolation,
    papers: https://www.researchgate.net/publication/366816309_Performance_Analysis_on_Interpolation-based_Methods_for_Fingerprint_Images,
    https://biometrics.cse.msu.edu/Publications/Fingerprint/MSU-CPS-97-35fenhance.pdf
    Based on first paper it is not the best interpolation type in context of fingerprint image processing,
    but not the worst too.
    Second paper gave me just the idea of interpolation based on neighbouring blocks (Section 2.5, point 5)
    :param _img_shape: Shape of image
    :param _block_size: Size of block that image is divided of
    :param theta: nparray with orientations of each block
    :return: Interpolated orientations
    """

    orientations = np.full(_img_shape, -1.0)  # Result array

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
    weights = weights / weights.sum(axis=0)  # normalized to 0, 1

    for j in range(y_blocks - 1):
        for i in range(x_blocks - 1):
            neighbours = np.array([
                theta[j, i],  # top-left
                theta[j + 1, i],  # bottom-left
                theta[j, i + 1],  # top-right
                theta[j + 1, i + 1]  # bottom-right
            ])

            complex_orientations = np.exp(2j * neighbours)
            interpolated_complex = np.sum(weights * complex_orientations[:, np.newaxis, np.newaxis], axis=0)

            interpolated_angles = np.angle(interpolated_complex) / 2

            j_slice = slice(j * _block_size + half_block_size, j * _block_size + half_block_size + _block_size)
            i_slice = slice(i * _block_size + half_block_size, i * _block_size + half_block_size + _block_size)
            orientations[j_slice, i_slice] = interpolated_angles

    return orientations
