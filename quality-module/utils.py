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


folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB')
tif_files = list(folder.glob('*.tif'))

for tif_file in tif_files:
    img = read_image(tif_file)
    show_image_on_plot(img, tif_file)
    plt.show()
    # plt.figure().suptitle('ez')
    # plt.imshow(img, cmap="gray")
    # plt.show()
