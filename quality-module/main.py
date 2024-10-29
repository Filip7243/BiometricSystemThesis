from pathlib import Path

import numpy as np
from PIL import Image
from matplotlib import pyplot as plt

import gabor_filter
import img_orientation
import ridge_frequency
import utils

# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB')
# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\SOCOFing\Real')
# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002\DB1_B_PNGS\classify\fair')
# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002\DB1_B_PNGS\classify\very_good')
folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB\pngs\classify\very_good')
# TODO: dla very_good 15.png sprawdzic jak wyglada gabor filter
# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002\DB1_B_PNGS\classify\good')
# TODO: teraz dla kazdej kalsy zrobic mean score i zobaczyc jak to ma odzwierciedelnie
# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB\images\500\png\plain')
tif_files = list(folder.glob('*.png'))


consistencies_values = []
coherences_values = []
frequency_values = []
gabor_values = []

for tif_file in tif_files[:100]:
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
    orientations, coherence = np.where(mask == 1.0, img_orientation.estimate_orientation(image, _interpolate=True),
                                       -1.0)

    coherences_values.append(np.mean(coherence[mask == 1.0]))
    # print(f'orents: {np.min(orientations)}, {np.max(orientations)}')
    # print(f'orents: {np.min(coherence)}, {np.max(coherence)}')

    # utils.showOrientations(image, orientations, 'Orientations', 8)  # TODO: gabor filter
    # plt.show()
    #
    # coh_img = plt.imshow(coherence, cmap='jet')
    # plt.colorbar(coh_img)
    # plt.title('coherence')
    # plt.axis('off')
    # plt.tight_layout()
    # plt.show()

    # print('Orientation consistency')
    # orientation_consistency = np.where(mask == 1.0,
    #                                    img_orientation.measure_orientation_consistency(image, orientations,
    #                                                                                    _block_size=16), -1.0)
    orientation_consistency = np.where(mask == 1.0,
                                       img_orientation.measure_orientation_consistency2(orientations), -1.0)

    consistencies_values.append(np.mean(orientation_consistency[mask == 1.0]))

    # plt.figure(figsize=(15, 5))
    #
    # # # Consistency map
    # # plt.subplot(131)
    # consistency_map = plt.imshow(orientation_consistency, cmap='jet', vmin=0, vmax=1)
    # plt.colorbar(consistency_map)
    # plt.title('Orientation Consistency')
    # plt.axis('off')
    #
    # plt.tight_layout()
    # plt.show()

    # print(f'const: {np.min(orientation_consistency)}, {np.max(orientation_consistency)}')
    #
    # print(
    #     f'mean: {np.mean(orientation_consistency[orientation_consistency > -1.0])}, '
    #     f'median:{np.median(orientation_consistency[orientation_consistency > -1.0])}, '
    #     f'std: {np.std(orientation_consistency[orientation_consistency > -1.0])}')

    # values = fuzzy_orientation_classification.normalize_input_values(coherence[mask == 1],
    #                                                                  orientation_consistency[mask == 1])
    #
    # fuzzy_orientation_classification.fuzzy_classification(values[0], values[1])
    #
    # plt.hist(orientation_consistency[orientation_consistency > -1.0].flatten(), bins=20)
    # plt.title('Histogram of Orientation Consistency')
    # plt.xlabel('Consistency')
    # plt.ylabel('Frequency')
    # plt.show()

    # TODO: for consistency there will be a*mean+b*std
    # TODO: otsu segmentation
    # utils.show_image_on_plot(orientation_consistency, 'Orientation consistency')

    print('Estimating frequencies')
    frequencies = np.where(mask == 1.0, ridge_frequency.estimate_frequencies(image, orientations), -1.0)
    frequency_values.append(frequencies[mask == 1.0])
    # imshow = plt.imshow(utils.normalize_image(frequencies), cmap='jet')
    # plt.colorbar(imshow)
    # utils.show_image_on_plot(utils.normalize_image(frequencies), 'Frequencies')
    print(f'freq: {np.min(frequencies[mask == 1.0])}, {np.max(frequencies[mask == 1.0])}')
    print(f'freq mean: {np.mean(frequencies[mask == 1.0])}, freq std: {np.std(frequencies[mask == 1.0])}')
    # map, score = fuzzy_ridge_frequency_classification.evaluate_fingerprint_quality(image, frequencies)
    #
    # print(
    #     f'Score: mean: {np.mean(map[mask == 1.0])}, std: {np.std(map[mask == 1.0])}, min: {np.min(map[mask == 1.0])}, max: {np.max(map[mask == 1.0])}')

    # (h, w) = image.shape
    # for j in range(0, h - 16 + 1, 16):
    #     for i in range(0, w - 16 + 1, 16):
    #         img_block = image[j:j + 16, i:i + 16]
    #         block_freq = frequencies[j + 16 // 2, i + 16 // 2]
    #         return_vals = fuzzy_ridge_frequency_classification.analyze_frequency_block(img_block, block_freq)
    #         print(return_vals)
    # quality_mask = np.full(frequencies.shape, -1)
    # valid_freq = (frequencies >= (1 / 25)) & (frequencies <= (1 / 3))
    # quality_mask[valid_freq] = 1
    # print(f'quality mask: {quality_mask.shape}')
    # print(f'freq_shape: {frequencies.shape}')
    # # utils.show_image_on_plot(quality_mask, 'Quality Mask')
    #
    print('Filtering')
    gbr = utils.normalize_image(gabor_filter.apply_gabor_filter(image, orientations, frequencies))
    print(
        f'Gabor: mean: {np.mean(gbr[mask == 1.0])}, std: {np.std(gbr[mask == 1.0])}, min: {np.min(gbr[mask == 1.0])}, max: {np.max(gbr[mask == 1.0])}, var: {np.var(gbr[mask == 1.0])}')  # TODO: ogaranc jak interpretowac ten gabor, moze na podstawie rdge, i orient ale idk
    gabor_values.append(gbr[mask == 1.0])
    gbr = np.where(mask == 1.0, gbr, 1.0)
    # utils.show_image_on_plot(gbr, "Gabor filter")
    #
    # plt.show()
    #

    # where = np.where(mask == 1.0,
    #                  img_quality.estimate_quality(orientations, orientation_consistency, frequencies, image), -2.0)

    # print(where[where > -2.0][0])

    # plt.show()

print('COHERENCE')
print(f'Mean: {np.mean(coherences_values)}')
print(f'Variance: {np.var(coherences_values)}')
print(f'Median: {np.median(coherences_values)}')
print(f'Std: {np.std(coherences_values)}')

print('ORIENTATION CONSISTENCY')
print(f'Mean: {np.mean(consistencies_values)}')
print(f'Variance: {np.var(consistencies_values)}')
print(f'Median: {np.median(consistencies_values)}')
print(f'Std: {np.std(consistencies_values)}')

print('RIDGE FREQ')
flat_frequency_values = np.concatenate(frequency_values)
print(f'Mean: {np.mean(flat_frequency_values)}')
print(f'Variance: {np.var(flat_frequency_values)}')
print(f'Median: {np.median(flat_frequency_values)}')
print(f'Std: {np.std(flat_frequency_values)}')

print('GABOR FILTER')
flat_gabor = np.concatenate(gabor_values)
print(f'Mean: {np.mean(flat_gabor)}')
print(f'Variance: {np.var(flat_gabor)}')
print(f'Median: {np.median(flat_gabor)}')
print(f'Std: {np.std(flat_gabor)}')

plt.figure(figsize=(10, 5))
plt.subplot(1, 4, 1)
plt.hist(consistencies_values, bins=20, color='blue', alpha=0.7)
plt.title('Orientation Consistency Distribution')
plt.xlabel('Consistency Value')
plt.ylabel('Frequency')

plt.subplot(1, 4, 2)
plt.hist(coherences_values, bins=20, color='green', alpha=0.7)
plt.title('Coherence Distribution')
plt.xlabel('Coherence Value')
plt.ylabel('Frequency')

plt.subplot(1, 4, 3)
plt.hist(flat_frequency_values, bins=20, color='green', alpha=0.7)
plt.title('Ridge frequency')
plt.xlabel('Freq Value')
plt.ylabel('Frequency')

plt.subplot(1, 4, 4)
plt.hist(flat_gabor, bins=20, color='green', alpha=0.7)
plt.title('Gabor')
plt.xlabel('Gabor Value')
plt.ylabel('Frequency')

plt.tight_layout()
plt.show()
