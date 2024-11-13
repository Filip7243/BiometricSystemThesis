import cv2
import numpy as np
from scipy.signal import convolve2d

import img_orientation
import ridge_frequency
import utils


def create_gabor_filter(_kernel_size, _angle, _frequency, x_sigma=4, y_sigma=4):
    """
    Function that creates gabor filter, based on given parameters estimated before applying the filter.
    Function created based on the paper: https://link.springer.com/chapter/10.1007/3-540-45344-X_39
    And GitHub code: https://github.com/tommythorsen/fingerprints/blob/master/gabor

    :param _kernel_size: Size of gabor filter kernel, commonly an odd number
    :param _angle: Orientation of the gabor filter based on estimated block orientation of fingerprint image (radians)
    :param _frequency: Sinusoidal wave frequency of the gabor filter based on estimated block ridge frequency
    :param x_sigma: standard deviation of the gabor filter kernel at x
    :param y_sigma: standard deviation of the gabor filter kernel at y
    :return: gabor filter to apply
    """
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


def apply_gabor_filter(_img, _orientations, _frequencies):
    h, w = _img.shape
    y, x = 0, 0

    filtered = np.empty((h, w))
    stack = [(y, x, h, w)]

    while stack:
        y, x, h, w = stack.pop()
        averaged_block_orientation, std = img_orientation.average_orientation(_orientations[y: y + h, x: x + w],
                                                                              _std=True)

        std_threshold = 0.2
        if (std < std_threshold and h < 50 and w < 50) or h < 6 or w < 6:
            neighbours = _frequencies[y: y + h, x: x + w]
            averaged_frequency = ridge_frequency.average_frequencies(neighbours)

            if averaged_frequency >= 0.0:
                kernel = create_gabor_filter(16, averaged_block_orientation, averaged_frequency)
                filtered[y: y + h, x: x + w] = convolve(_img, kernel, (y, x), (h, w))
            else:
                filtered[y: y + h, x: x + w] = _img[y: y + h, x: x + w]
        else:
            if h > w:
                mid_h = h // 2
                stack.append((y, x, mid_h, w))
                stack.append((y + mid_h, x, h - mid_h, w))
            else:
                mid_w = w // 2
                stack.append((y, x, h, mid_w))
                stack.append((y, x + mid_w, h, w - mid_w))

    if w > 20 and h > 20:
        filtered = utils.normalize(filtered)

    return filtered


def convolve(_img, _kernel, _origin=(0, 0), _shape=None):
    """
    Function that convolve image with gabor filter.
    :param _img: Input image
    :param _kernel: Gabor filter kernel created from function 'create_gabor_filter()'
    :param _origin: Starting point of gabor filter
    :param _shape: Area where gabor will be applied
    :return: Convolved image
    """
    if _shape is None:
        _shape = (_img.shape[0] - _origin[0], _img.shape[1] - _origin[1])

    kernel_h, kernel_w = _kernel.shape
    kernel_y, kernel_x = -(kernel_h // 2), -(kernel_w // 2)

    # Find padding to avoid index out of bound
    top_pad = max(0, -(_origin[0] + kernel_y))
    left_pad = max(0, -(_origin[1] + kernel_x))
    bottom_pad = max(0, (_origin[0] + _shape[0] + kernel_y + kernel_h) - _img.shape[0])
    right_pad = max(0, (_origin[1] + _shape[1] + kernel_x + kernel_w) - _img.shape[1])

    padding = (top_pad, bottom_pad), (left_pad, right_pad)
    if np.any(padding):
        _img = np.pad(_img, padding, mode='edge')

    padded_y = top_pad + _origin[0] + kernel_y
    padded_x = left_pad + _origin[1] + kernel_x

    result = np.empty(_shape)
    for j in range(_shape[0]):
        row_start = padded_y + j
        row_end = row_start + kernel_h
        for i in range(_shape[1]):
            col_start = padded_x + i
            col_end = col_start + kernel_w

            img_block = _img[row_start:row_end, col_start:col_end]
            result[j, i] = np.sum(img_block * _kernel)

    return result


def create_gabor_kernel(_kernel_size, function):
    """
    Function that creates a gabor kernel using function passed as argument.
    Code taken from: https://github.com/tommythorsen/fingerprints/blob/master/utils.py
    :param _kernel_size: Size of kernel, for example: 3 means matrix size 3x3
    :param function: Function that creates a gabor kernel
    :return:
    """

    kernel = np.empty((_kernel_size, _kernel_size))
    for j in range(0, _kernel_size):
        for i in range(0, _kernel_size):
            kernel[j, i] = function(j - _kernel_size / 2, i - _kernel_size / 2)

    return kernel
