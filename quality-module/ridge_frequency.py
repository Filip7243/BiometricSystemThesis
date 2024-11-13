import cv2
import numpy as np
from scipy import signal, ndimage

import utils


def estimate_frequencies(_img, _orientations, _block_size=32, _min_wave_length=5, _max_wave_length=15):
    """
    Estimate _img ridge frequencies based on _orientations. Function created base on matlab code:
    https://github.com/noureldien/FingerprintRecognition/blob/master/Matlab/RidgeFilter/freqest.m
    And paper: https://biometrics.cse.msu.edu/Publications/Fingerprint/MSU-CPS-97-35fenhance.pdf
    :param _img: Input image
    :param _orientations: Orientations of the image
    :param _block_size: Block size that we estimate ridge frequency for
    :param _min_wave_length: Minimum ridge wave length in pixels (default 5 is suggested in matlab code)
    :param _max_wave_length: Maximum ridge wave length in pixels (default 15 is suggested in matlab code)
    :return: Frequencies for each block
    """
    (h, w) = _img.shape
    y_blocks, x_blocks = h // _block_size, w // _block_size
    frequencies = np.full((y_blocks, x_blocks), -1.0)

    for j in range(y_blocks):
        for i in range(x_blocks):
            y_slice = j * _block_size + _block_size // 2  # Horizontal center
            x_slice = i * _block_size + _block_size // 2  # Vertical center
            block_orientation = _orientations[y_slice, x_slice]

            y_start, y_end = j * _block_size, (j + 1) * _block_size
            x_start, x_end = i * _block_size, (i + 1) * _block_size
            block_img = _img[y_start: y_end, x_start: x_end]
            # block_img = rotate_and_crop(block_img, np.pi * 0.5 + block_orientation)
            block_img = rotate_and_crop2(block_img, np.pi * 0.5 + block_orientation)

            # Skip if block is empty
            if block_img.size == 0:
                continue

            columns = np.sum(block_img, axis=0)
            columns = utils.normalize(columns)

            # Finding ridges by peaks in columns, min distance=3(like in paper)
            peaks = signal.find_peaks_cwt(columns, np.array([3]))
            if len(peaks) >= 2:
                f = (peaks[-1] - peaks[0]) / (len(peaks) - 1)  # Average spacing between all ridges
                if _min_wave_length <= f <= _max_wave_length:
                    frequencies[j, i] = 1 / f

    result_freq = np.full(_img.shape, -1.0)
    frequencies = np.pad(frequencies, 1, 'edge')
    for j in range(y_blocks):
        for i in range(x_blocks):
            neighbours = frequencies[j: j + 3, i: i + 3]  # 3x3 block
            valid_neighbours = neighbours[neighbours >= 0]
            if valid_neighbours.size > 0:
                y_start, y_end = j * _block_size, (j + 1) * _block_size
                x_start, x_end = i * _block_size, (i + 1) * _block_size
                result_freq[y_start: y_end, x_start: x_end] = np.median(valid_neighbours)

    return result_freq


def rotate_and_crop(_block_image, _angle):
    """
    Rotate given _block_img by _angle in radians.
    Code taken from: https://github.com/tommythorsen/fingerprints/blob/master/utils.py
    :param _block_image: Input image
    :param _angle: Angle of rotation in radians
    :return: Rotated image
    """
    (h, w) = _block_image.shape
    is_landscape = w >= h
    long_side, short_side = (w, h) if is_landscape else (h, w)

    sin, cos = abs(np.sin(_angle)), abs(np.cos(_angle))
    sin2a = 2.0 * sin * cos

    if short_side <= sin2a * long_side:
        x = 0.5 * short_side
        new_w, new_h = (x / sin, x / cos) if is_landscape else (x / cos, x / sin)
    else:
        cos2a = cos ** 2 - sin ** 2
        new_w = (w * cos - h * sin) / cos2a
        new_h = (h * cos - w * sin) / cos2a

    rotated_image = ndimage.interpolation.rotate(_block_image, np.degrees(_angle), reshape=False)

    new_h, new_w = int(new_h), int(new_w)
    y, x = (h - new_h) // 2, (w - new_w) // 2

    return rotated_image[y: y + new_h, x: x + new_w]


def rotate_and_crop2(block: np.ndarray, angle: float):
    center = (block.shape[1] / 2, block.shape[0] / 2)
    rotation_matrix = cv2.getRotationMatrix2D(
        center,
        angle * 180 / np.pi,
        1.0
    )
    return cv2.warpAffine(
        block,
        rotation_matrix,
        (block.shape[1], block.shape[0])
    )


def average_frequencies(_frequencies):
    """
    Function that averages ridge frequencies across blocks with np library
    :param _frequencies: Block frequencies
    :return: Averaged ridge frequencies
    """

    frequencies = _frequencies[_frequencies >= 0]
    if frequencies.size == 0:
        return -1
    return np.average(frequencies)
