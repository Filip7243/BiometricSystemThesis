from pathlib import Path

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

# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB')
folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB\images\500\png\plain')
tif_files = list(folder.glob('*.png'))

for tif_file in tif_files:
    img = read_image(tif_file)
    show_image_on_plot(img, tif_file)

    norm_img = normalize_image(img)
    show_image_on_plot(norm_img, tif_file)
    print(np.min(norm_img), np.max(norm_img))
    plt.show()
