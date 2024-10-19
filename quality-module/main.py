from pathlib import Path

import numpy as np
from matplotlib import pyplot as plt

import gabor_filter
import img_orientation
import ridge_frequency
import utils
import imageio

folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB')
# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB\images\500\png\plain')
tif_files = list(folder.glob('*.tif'))

for tif_file in tif_files:
    print('Reading image')
    image = utils.read_image(tif_file)
    utils.show_image_on_plot(image, 'Original Image')

    print('Normalization')
    image = utils.normalize_image(image)
    utils.show_image_on_plot(image, 'Normalized Image')

    print('Finding Mask')
    mask = utils.segment_fingerprint(image)
    utils.show_image_on_plot(mask, 'Mask fingerprint')

    print('Estimating orientations/coherence')
    orientations, coherence = img_orientation.estimate_orientation(image, _interpolate=True)
    orientations = np.where(mask == 1.0, orientations, -1.0)

    utils.showOrientations(image, orientations, 'Orientations', 8)  # TODO: gabor filter

    print('Orientation consistency')
    orientation_consistency = np.where(mask == 1.0,
                                       img_orientation.measure_orientation_consistency(image, orientations,
                                                                                       _block_size=16), -1.0)

    # consistency_map = plt.imshow(orientation_consistency, cmap='jet')
    # plt.colorbar(consistency_map)
    # plt.title('Orientation Consistency')
    # plt.axis('off')

    print('Estimating frequencies')
    frequencies = np.where(mask == 1.0, ridge_frequency.estimate_frequencies(image, orientations), -1.0)
    utils.show_image_on_plot(utils.normalize_image(frequencies), 'Frequencies')

    print('Filtering')
    image = utils.normalize_image(gabor_filter.apply_gabor_filter(image, orientations, frequencies))
    image = np.where(mask == 1.0, image, 1.0)
    utils.show_image_on_plot(image, "Gabor filter")

    plt.show()
