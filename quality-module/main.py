from pathlib import Path

import numpy as np
from matplotlib import pyplot as plt

import gabor_filter
import img_orientation
import ridge_frequency
import utils
import img_quality

folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB')
# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB\images\500\png\plain')
tif_files = list(folder.glob('*.png'))

for tif_file in tif_files:
    print(tif_file)
    print('Reading image')
    image = utils.read_image(tif_file)
    # utils.show_image_on_plot(image, 'Original Image')

    print('Normalization')
    image = utils.normalize_image(image)
    # utils.show_image_on_plot(image, 'Normalized Image')

    print('Finding Mask')
    mask = utils.segment_fingerprint(image)
    # utils.show_image_on_plot(mask, 'Mask fingerprint')

    print('Estimating orientations/coherence')
    orientations, coherence = img_orientation.estimate_orientation(image, _interpolate=True)
    orientations = np.where(mask == 1.0, orientations, -1.0)
    print(orientations[orientations == -1])

    # utils.showOrientations(image, orientations, 'Orientations', 8)  # TODO: gabor filter

    print('Orientation consistency')
    orientation_consistency = np.where(mask == 1.0,
                                       img_orientation.measure_orientation_consistency(image, orientations,
                                                                                       _block_size=16), -1.0)

    # utils.show_image_on_plot(orientation_consistency, 'Orientation consistency')

    print('Estimating frequencies')
    frequencies = np.where(mask == 1.0, ridge_frequency.estimate_frequencies(image, orientations), -1.0)
    # utils.show_image_on_plot(utils.normalize_image(frequencies), 'Frequencies')
    print(f'freq: {np.min(frequencies)}, {np.max(frequencies)}')
    quality_mask = np.full(frequencies.shape, -1)
    valid_freq = (frequencies >= (1 / 25)) & (frequencies <= (1 / 3))
    quality_mask[valid_freq] = 1
    print(f'quality mask: {quality_mask.shape}')
    print(f'freq_shape: {frequencies.shape}')
    # utils.show_image_on_plot(quality_mask, 'Quality Mask')

    print('Filtering')
    image = utils.normalize_image(gabor_filter.apply_gabor_filter(image, orientations, frequencies))
    image = np.where(mask == 1.0, image, 1.0)
    # utils.show_image_on_plot(image, "Gabor filter")

    where = np.where(mask == 1.0,
                     img_quality.estimate_quality(orientations, orientation_consistency, frequencies, image), -2.0)

    # print(where[where > -2.0][0])

    plt.show()
