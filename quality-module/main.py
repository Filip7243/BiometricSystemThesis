from pathlib import Path

import numpy as np
from matplotlib import pyplot as plt

import gabor_filter
import img_orientation
import ridge_frequency
import utils

folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB')
# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB\images\500\png\plain')
tif_files = list(folder.glob('*.tif'))

for tif_file in tif_files:
    img = utils.read_image(tif_file)
    utils.show_image_on_plot(img, tif_file)

    norm_img = utils.normalize_image(img)
    # show_image_on_plot(norm_img, tif_file)

    mask = utils.segment_fingerprint(norm_img)
    # mask2 = img_segmentation.create_fingerprint_mask(img, k=16)  # TODO: imporve it, it works too long
    utils.show_image_on_plot(mask, tif_file)
    # show_image_on_plot(mask2, tif_file)

    est_orientation, coh = img_orientation.estimate_orientation(norm_img, _interpolate=True)

    consistency = img_orientation.measure_orientation_consistency(norm_img, est_orientation, _block_size=16)
    where = np.where(mask == 1.0, consistency, -1.0)
    result = np.clip(where, 0, 1)

    # frequencies, median_frequency = ridge_frequency.estimate_ridge_frequency(_img=norm_img, _mask=mask,
    #                                                                          _orientations=est_orientation,
    #                                                                          _block_size=32,
    #                                                                          _window_size=5,
    #                                                                          _min_wave_len=5, _max_wave_len=15)

    frequencies = np.where(mask == 1.0, ridge_frequency.estimate_frequencies(norm_img, est_orientation), -1.0)
    utils.show_image_on_plot(utils.normalize_image(frequencies), "frequencies")  # TODO: gabor i essunia

    non_zero_freq = frequencies[frequencies > 0]
    print(f"Min frequency: {non_zero_freq.min()}")
    print(f"Max frequency: {non_zero_freq.max()}")
    print(f"Mean frequency: {non_zero_freq.mean()}")
    print(f"Median frequency: {np.median(non_zero_freq)}")

    im = utils.normalize_image(gabor_filter.apply_gabor_filter(norm_img, est_orientation, frequencies))
    im = np.where(mask == 1.0, im, 1.0)
    utils.show_image_on_plot(im, "gabor_filter")

    # where = where[where >= 0]
    # mean_consistency = np.mean(where)
    # print(f'mean_consistency: {mean_consistency}')
    # high_consistency_count = np.sum(where > 0.5)
    # print(f'high_consistency_count: {high_consistency_count}')
    # total_pixels = where.size
    # print(f'total_pixels: {total_pixels}')
    # proportion_high_consistency = high_consistency_count / total_pixels
    # print(f'proportion_high_consistency: {proportion_high_consistency}')
    # quality_score = (mean_consistency + proportion_high_consistency) / 2
    # print(f'quality_score: {quality_score}')

    # consistency_map = plt.imshow(result, cmap='jet')
    # plt.colorbar(consistency_map)
    # plt.title('Orientation Consistency')
    # plt.axis('off')
    # plt.show()

    # orientations = np.where(mask == 1.0, est_orientation, -1.0)

    # cohs = np.where(mask == 1.0, coh, -1.0)

    # valid_coherence = cohs[cohs >= 0]
    # good_regions = np.sum(valid_coherence > 0.65) / len(valid_coherence)
    # mean_coherence = np.mean(valid_coherence)
    # weighted_score = np.sum(valid_coherence ** 2) / len(valid_coherence)
    # combined_score = 0.4 * good_regions + 0.3 * mean_coherence + 0.3 * weighted_score

    # plt.imshow(cohs, cmap='hot')
    # plt.colorbar(label='Coherence')
    # plt.title('Coherence Map')
    # plt.show()
    #
    # showOrientations(img, orientations, "orientations", 8)

    # TODO: estimate frequncie, segmentaion mask, coherence, consistency

    plt.show()
