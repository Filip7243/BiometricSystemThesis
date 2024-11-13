#!/usr/bin/env python3
from pathlib import Path

import matplotlib.pyplot as plt
import numpy as np
import imageio

import utils


def gaborKernel(size, angle, frequency):
    """
    Create a Gabor kernel given a size, angle and frequency.

    Code is taken from https://github.com/rtshadow/biometrics.git
    """

    angle += np.pi * 0.5
    cos = np.cos(angle)
    sin = -np.sin(angle)

    yangle = lambda x, y: x * cos + y * sin
    xangle = lambda x, y: -x * sin + y * cos

    xsigma = ysigma = 4

    return utils.kernelFromFunction(size, lambda x, y:
    np.exp(-(
            (xangle(x, y) ** 2) / (xsigma ** 2) +
            (yangle(x, y) ** 2) / (ysigma ** 2)) / 2) *
    np.cos(2 * np.pi * frequency * xangle(x, y)))


def gaborFilter(image, orientations, frequencies, w=32):
    result = np.empty(image.shape)

    height, width = image.shape
    for y in range(0, height - w, w):
        for x in range(0, width - w, w):
            orientation = orientations[y + w // 2, x + w // 2]
            frequency = utils.averageFrequency(frequencies[y:y + w, x:x + w])

            if frequency < 0.0:
                result[y:y + w, x:x + w] = image[y:y + w, x:x + w]
                continue

            kernel = gaborKernel(16, orientation, frequency)
            result[y:y + w, x:x + w] = utils.convolve(image, kernel, (y, x), (w, w))

    return utils.normalize(result)


def gaborFilterSubdivide(image, orientations, frequencies, rect=None):
    if rect:
        y, x, h, w = rect
    else:
        y, x = 0, 0
        h, w = image.shape

    result = np.empty((h, w))

    orientation, deviation = utils.averageOrientation(
        orientations[y:y + h, x:x + w], deviation=True)

    if (deviation < 0.2 and h < 50 and w < 50) or h < 6 or w < 6:

        frequency = utils.averageFrequency(frequencies[y:y + h, x:x + w])

        if frequency < 0.0:
            result = image[y:y + h, x:x + w]
        else:
            kernel = gaborKernel(16, orientation, frequency)
            result = utils.convolve(image, kernel, (y, x), (h, w))

    else:
        if h > w:
            hh = h // 2

            result[0:hh, 0:w] = \
                gaborFilterSubdivide(image, orientations, frequencies, (y, x, hh, w))

            result[hh:h, 0:w] = \
                gaborFilterSubdivide(image, orientations, frequencies, (y + hh, x, h - hh, w))
        else:
            hw = w // 2

            result[0:h, 0:hw] = \
                gaborFilterSubdivide(image, orientations, frequencies, (y, x, h, hw))

            result[0:h, hw:w] = \
                gaborFilterSubdivide(image, orientations, frequencies, (y, x + hw, h, w - hw))

    if w > 20 and h > 20:
        result = utils.normalize(result)

    return result


if __name__ == '__main__':
    np.set_printoptions(
        threshold=np.inf,
        precision=4,
        suppress=True)

    folder = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\analized_data')
    quality_folders = [f for f in folder.iterdir() if f.is_dir()]

    for quality_folder in quality_folders[5:]:
        quality_class = quality_folder.name
        files = list(quality_folder.glob('*.png'))

        for file in files:
            print("Reading image")
            image = imageio.v2.imread(file).astype("float64")
            utils.showImage(image, "original", vmax=255.0)

            print("Normalizing")
            image = utils.normalize(image)
            utils.showImage(image, "normalized")

            print(f'After norm: {np.min(image)}, {np.max(image)}')

            print("Finding mask")
            mask = utils.findMask(image)
            utils.showImage(mask, "mask")

            print("Applying local normalization")
            image = np.where(mask == 1.0, utils.localNormalize(image), image)
            utils.showImage(image, "locally normalized")

            print(f'After local norm: {np.min(image)}, {np.max(image)}')

            print("Estimating orientations")
            orientations = utils.estimateOrientations(image)
            print(f'min: {np.min(orientations)}, max: {np.max(orientations)}')
            orientations = np.where(mask == 1.0, orientations, -1.0)

            print(f'min: {np.min(orientations[mask == 1.0])}, max: {np.max(orientations[mask == 1.0])}')

            # utils.showOrientations(image, orientations, "orientations", 8)

            print("Estimating frequencies")
            frequencies = np.where(mask == 1.0, utils.estimateFrequencies(image, orientations), -1.0)
            utils.showImage(utils.normalize(frequencies), "frequencies")

            print(f'After frequencies: {np.min(frequencies)}, {np.max(frequencies)}')

            print("Filtering")
            image = utils.normalize(gaborFilterSubdivide(image, orientations, frequencies))
            # image = gaborFilter(image, orientations, frequencies)

            image = np.where(mask == 1.0, image, 1.0)

            print(f'After gabor: {np.min(image)}, {np.max(image)}')

            utils.showImage(image, "gabor")

            image = np.where(mask == 1.0, utils.binarize(image), 1.0)
            utils.showImage(image, "binarized")

            plt.show()
