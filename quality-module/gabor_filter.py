import numpy as np

import img_orientation
import ridge_frequency
from scipy.signal import convolve2d


def create_gabor_filter(_kernel_size, _angle, _frequency, x_sigma=4, y_sigma=4):
    _angle += np.pi * 0.5
    cos = np.cos(_angle)
    sin = -np.sin(_angle)

    def rotate_x(x, y):
        return x * cos + y * sin

    def rotate_y(x, y):
        return -x * sin + y * cos

    def gabor(x, y):
        gauss = np.exp(-((rotate_y(x, y) ** 2) / (x_sigma ** 2) +
                         (rotate_x(x, y) ** 2) / (y_sigma ** 2)) / 2)
        wave = np.cos(2 * np.pi * _frequency * rotate_y(x, y))
        return gauss * wave

    return create_gabor_kernel(_kernel_size, gabor)


def apply_gabor_filter(_img, _orientations, _frequencies, region=None):
    if region:
        y, x, h, w = region
    else:
        (h, w) = _img.shape
        y, x = 0, 0

    filtered = np.empty((h, w))

    block_orientation, std = img_orientation.average_orientation(_orientations[y: y + h, x: x + w], _std=True)

    std_threshold = 0.2  # Small std = consistence orientation
    # If region to filter is small enough, do filtering
    if (std < std_threshold and h < 50 and w < 50) or h < 6 or w < 6:
        neighbours = _frequencies[y: y + h, x: x + w]
        averaged_frequency = ridge_frequency.average_frequencies(neighbours)

        if averaged_frequency >= 0.0:
            kernel = create_gabor_filter(16, block_orientation, averaged_frequency)
            filtered = convolve(_img, kernel, (y, x), (h, w))
        else:
            filtered = _img[y: y + h, x: x + w]  # No filter
    else:  # Subdivide region to smaller blocks, then apply filter
        if h > w:  # Portrait
            half_height = h // 2
            filtered[0:half_height, 0:w] = apply_gabor_filter(_img, _orientations, _frequencies, (y, x, half_height, w))
            filtered[half_height:h, 0:w] = apply_gabor_filter(_img, _orientations, _frequencies,
                                                              (y + half_height, x, h - half_height, w))
        else:  # Landscape
            half_width = w // 2
            filtered[0:h, 0:half_width] = apply_gabor_filter(_img, _orientations, _frequencies, (y, x, h, half_width))
            filtered[0:h, half_width:w] = apply_gabor_filter(_img, _orientations, _frequencies,
                                                             (y, x + half_width, h, w - half_width))

    if w > 20 and h > 20:
        _frequencies = normalize_image(filtered)

    return filtered


def convolve(_img, _kernel, _origin=(0, 0), _shape=None):
    if _shape is None:
        _shape = (_img.shape[0] - _origin[0], _img.shape[1] - _origin[1])

    result = np.empty(_shape)

    kernel_height, kernel_width = _kernel.shape

    kernel_origin_y, kernel_origin_x = -(kernel_height // 2), -(kernel_width // 2)

    top_pad = max(0, -(_origin[0] + kernel_origin_y))
    left_pad = max(0, -(_origin[1] + kernel_origin_x))
    bottom_pad = max(0, (_origin[0] + _shape[0] + kernel_origin_y + kernel_height) - _img.shape[0])
    right_pad = max(0, (_origin[1] + _shape[1] + kernel_origin_x + kernel_width) - _img.shape[1])

    padding = (top_pad, bottom_pad), (left_pad, right_pad)
    if np.any(padding):
        _img = np.pad(_img, padding, mode='edge')

    padded_origin_y = top_pad + _origin[0] + kernel_origin_y
    padded_origin_x = left_pad + _origin[1] + kernel_origin_x

    for j in range(_shape[0]):
        for i in range(_shape[1]):
            img_block = _img[padded_origin_y + j:padded_origin_y + j + kernel_height,
                        padded_origin_x + i:padded_origin_x + i + kernel_width]

            result[j, i] = np.sum(img_block * _kernel)

    return result


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


def create_gabor_kernel(_kernel_size, function):
    kernel = np.empty((_kernel_size, _kernel_size))
    for j in range(0, _kernel_size):
        for i in range(0, _kernel_size):
            kernel[j, i] = function(j - _kernel_size / 2, i - _kernel_size / 2)

    return kernel
