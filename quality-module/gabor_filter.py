import numpy as np

import img_orientation
import ridge_frequency


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
            # filtered = convolve(_img, kernel, (y, x), (h, w))
            filtered = convolve(_img[y:y + h, x:x + w], kernel)
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


def convolve(image, kernel, origin=None, shape=None, pad=True):
    """
    Apply a kernel to an image or to a part of an image.

    :param image:   The source image.
    :param kernel:  The kernel (an ndarray of black and white, or grayvalues).
    :param origin:  The origin of the part of the image to be convolved.
                    Defaults to (0, 0).
    :param shape:   The shape of the part of the image that is to be convolved.
                    Defaults to the shape of the image.
    :param pad:     Whether the image should be padded before applying the
                    kernel. Passing False here will cause indexing errors if
                    the kernel is applied at the edge of the image.
    :returns:       The resulting image.
    """
    if not origin:
        origin = (0, 0)

    if not shape:
        shape = (image.shape[0] - origin[0], image.shape[1] - origin[1])

    result = np.empty(shape)

    if callable(kernel):
        k = kernel(0, 0)
    else:
        k = kernel

    kernelOrigin = (-k.shape[0] // 2, -k.shape[1] // 2)
    kernelShape = k.shape

    topPadding = 0
    leftPadding = 0

    if pad:
        topPadding = max(0, -(origin[0] + kernelOrigin[0]))
        leftPadding = max(0, -(origin[1] + kernelOrigin[1]))
        bottomPadding = max(
            0,
            (origin[0] + shape[0] + kernelOrigin[0] + kernelShape[0]) - image.shape[0],
        )
        rightPadding = max(
            0,
            (origin[1] + shape[1] + kernelOrigin[1] + kernelShape[1]) - image.shape[1],
        )

        padding = (topPadding, bottomPadding), (leftPadding, rightPadding)

        if np.max(padding) > 0.0:
            image = np.pad(image, padding, mode="edge")

    for y in range(shape[0]):
        for x in range(shape[1]):
            iy = topPadding + origin[0] + y + kernelOrigin[0]
            ix = leftPadding + origin[1] + x + kernelOrigin[1]

            block = image[iy: iy + kernelShape[0], ix: ix + kernelShape[1]]
            if callable(kernel):
                result[y, x] = np.sum(block * kernel(y, x))
            else:
                result[y, x] = np.sum(block * kernel)

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
