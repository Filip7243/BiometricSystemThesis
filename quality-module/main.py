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
    print(f'orents: {np.min(orientations)}, {np.max(orientations)}')

    utils.showOrientations(image, orientations, 'Orientations', 8)  # TODO: gabor filter

    print('Orientation consistency')
    # orientation_consistency = np.where(mask == 1.0,
    #                                    img_orientation.measure_orientation_consistency(image, orientations,
    #                                                                                    _block_size=16), -1.0)
    orientation_consistency = np.where(mask == 1.0,
                                       img_orientation.measure_orientation_consistency2(orientations), -1.0)

    plt.figure(figsize=(15, 5))

    # # Consistency map
    # plt.subplot(131)
    # consistency_map = plt.imshow(orientation_consistency, cmap='jet', vmin=0, vmax=1)
    # plt.colorbar(consistency_map)
    # plt.title('Orientation Consistency')
    # plt.axis('off')
    #
    # plt.tight_layout()
    # plt.show()

    print(f'const: {np.min(orientation_consistency)}, {np.max(orientation_consistency)}')

    print(
        f'mean: {np.mean(orientation_consistency[orientation_consistency > -1.0])}, '
        f'median:{np.median(orientation_consistency[orientation_consistency > -1.0])}, '
        f'std: {np.std(orientation_consistency[orientation_consistency > -1.0])}')
    #
    # plt.hist(orientation_consistency[orientation_consistency > -1.0].flatten(), bins=20)
    # plt.title('Histogram of Orientation Consistency')
    # plt.xlabel('Consistency')
    # plt.ylabel('Frequency')
    # plt.show()

    # TODO: for consistency there will be a*mean+b*std
    # utils.show_image_on_plot(orientation_consistency, 'Orientation consistency')

    print('Estimating frequencies')
    frequencies = np.where(mask == 1.0, ridge_frequency.estimate_frequencies(image, orientations), -1.0)
    # utils.show_image_on_plot(utils.normalize_image(frequencies), 'Frequencies')
    print(f'freq: {np.min(frequencies)}, {np.max(frequencies)}')
    print(f'freq mean: {np.mean(frequencies[frequencies > -1.0])}, freq std: {np.std(frequencies[frequencies > -1.0])}')
    quality_mask = np.full(frequencies.shape, -1)
    valid_freq = (frequencies >= (1 / 25)) & (frequencies <= (1 / 3))
    quality_mask[valid_freq] = 1
    print(f'quality mask: {quality_mask.shape}')
    print(f'freq_shape: {frequencies.shape}')
    # utils.show_image_on_plot(quality_mask, 'Quality Mask')

    print('Filtering')
    image = utils.normalize_image(gabor_filter.apply_gabor_filter(image, orientations, frequencies))
    image = np.where(mask == 1.0, image, 1.0)
    utils.show_image_on_plot(image, "Gabor filter")

    # where = np.where(mask == 1.0,
    #                  img_quality.estimate_quality(orientations, orientation_consistency, frequencies, image), -2.0)

    # print(where[where > -2.0][0])

    plt.show()
