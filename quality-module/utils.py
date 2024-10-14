from pathlib import Path

import cv2
import imageio
import numpy as np
from matplotlib import pyplot as plt


def read_image(_img_path):
    """
    Function to read the image as numpy array of floats
    :param _img_path: Path of input fingerprint image
    :return: numpy array of floats
    """

    return imageio.v3.imread(_img_path).astype(np.float64)


def show_image_on_plot(_img, _title=None):
    """
    Function to show the image in grayscale on plot
    :param _img: Read image
    :param _title: Title of subplot of plot
    """

    plt.figure().suptitle(_title)
    plt.imshow(_img, cmap='gray')


def normalize_image(_img):
    """
    Function that normalizes image to values in range [0, 1]
    with max normalization from: https://research.ijcaonline.org/volume32/number10/pxc3875530.pdf section 4.5
    :param _img: Input fingerprint image
    :return: Normalized image between [0, 1]
    """
    _img = np.copy(_img)

    max_val = np.max(_img)
    if max_val > 0.0:
        _img /= max_val

    return _img


def segment_fingerprint(_img, _block_size=16, _threshold=0.3):
    (y, x) = _img.shape
    _threshold *= np.std(_img)

    _img_var = np.zeros_like(_img)
    mask = np.ones_like(_img)

    for i in range(0, x, _block_size):
        for j in range(0, y, _block_size):
            box = [i, j, min(i + _block_size, x), min(j + _block_size, y)]
            block_stddev = np.std(_img[box[1]:box[3], box[0]:box[2]])
            _img_var[box[1]:box[3], box[0]:box[2]] = block_stddev

    mask[_img_var < _threshold] = 0

    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (_block_size * 2, _block_size * 2))
    mask = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel)
    mask = cv2.morphologyEx(mask, cv2.MORPH_CLOSE, kernel)  # TODO: add padding to mask

    return mask


folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB')
# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB\images\500\png\plain')
tif_files = list(folder.glob('*.tif'))

for tif_file in tif_files:
    img = read_image(tif_file)
    show_image_on_plot(img, tif_file)

    norm_img = normalize_image(img)
    show_image_on_plot(norm_img, tif_file)

    fingerprint = segment_fingerprint(norm_img)
    show_image_on_plot(fingerprint, tif_file)

    plt.show()
