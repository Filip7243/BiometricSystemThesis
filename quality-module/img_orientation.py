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


def estimate_orientation(_img, _block_size=16, interpolate=False):
    (h, w) = _block_size

    # Smooth input image
    _img = ndimage.filters.gaussian_filter(_img, 2.0)

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

    # Adjust theta by adding 90 degrees (π/2) and take modulo π to ensure it stays within the range [0, π)
    theta = (theta + np.pi * 0.5) % np.pi

