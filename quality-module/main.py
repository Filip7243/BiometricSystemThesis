import csv
import os
from pathlib import Path

import cv2
import numpy as np
from matplotlib import pyplot as plt

import gabor_filter
import img_orientation
import ridge_frequency
import utils
import time

folder = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\analized_data')
quality_folders = [f for f in folder.iterdir() if f.is_dir()]

# TODO: posprzatac kod, i zaimplemnotwac wszystki funkcje z copilota i poczytac o kazdej z nich, porobic pozniej statystyki csv, przeanaalizowac dane i zrobic membershipa i essa
for quality_folder in quality_folders[5:]:

    quality_class = quality_folder.name
    files = list(quality_folder.glob('*.png'))

    consistencies_values = []
    coherences_values = []
    frequency_values = []
    gabor_values = []
    clarity_means = []
    clarity_stds = []
    freq_strenghts = []

    for file in files:
        start_time = time.time()

        original = utils.read_image(file)

        utils.show_image_on_plot(original)
        plt.show()

        mask = utils.segment_fingerprint(original)

        cnr = img_orientation.compute_cnr(original, mask)
        print(f'CNR: {cnr}')

        image = utils.normalize_image(original)

        orientations, coherence = img_orientation.estimate_orientation(image, _interpolate=True)
        locl = coherence / np.max(coherence)
        coherence = np.where(mask == 1.0, coherence, -1.0)
        print(f'LLC: {np.mean(locl[mask == 1.0])}')
        plt.imshow(locl, cmap='hot')
        plt.colorbar()
        plt.title('Local Orientation Certainty Level (LOCL)')
        plt.show()

        pdf_orint = img_orientation.compute_metric_error_pdf(orientations)
        pdf_coh = img_orientation.compute_metric_error_pdf(coherence)
        print(f'PDF orient: {pdf_orint}')
        print(f'PDF coh: {pdf_coh}')

        print(f'After orient: {np.min(orientations)}, {np.max(orientations)}')
        orientations = np.where(mask == 1.0, orientations, -1.0)

        frequencies = np.where(mask == 1.0, ridge_frequency.estimate_frequencies(image, orientations, 16), -1.0)
        pdf_freq = img_orientation.compute_metric_error_pdf(frequencies)
        print(f'PDF pdf_freq: {pdf_freq}')

        gbr = utils.normalize(gabor_filter.apply_gabor_filter(image, orientations, frequencies))
        snr = img_orientation.compute_snr(utils.normalize_image(original), gbr, mask)
        pdf_gab = img_orientation.compute_metric_error_pdf(gbr)
        gbr = np.where(mask == 1.0, gbr, 1.0)
        utils.show_image_on_plot(gbr)
        plt.show()
        end_time = time.time()
        elapsed_time = end_time - start_time
        print(f'Gabor mean masked: {np.mean(gbr[mask == 1.0])}')
        print(f'min-max gbr: {np.min(gbr)}, {np.max(gbr)}')
        print(f'SNR: {snr}')
        print(f'PDF pdf_gab: {pdf_gab}')
        print(f"Time taken for the Gabor filter operation: {elapsed_time:.4f} seconds")

    print('COHERENCE')
    print(f'Mean: {np.mean(coherences_values)}')
    print(f'Variance: {np.var(coherences_values)}')
    print(f'Median: {np.median(coherences_values)}')
    print(f'Std: {np.std(coherences_values)}')

    print('Clarity')
    print(f'Mean: {np.mean(clarity_means)}')
    print(f'Std: {np.mean(clarity_stds)}')

    print("Freq Strnegth")
    # flat_freq_strenghts = np.concatenate(freq_strenghts)
    print(f'Mean: {np.mean(freq_strenghts)}')
    print(f'Std: {np.std(freq_strenghts)}')

    print('ORIENTATION CONSISTENCY')
    print(f'Mean: {np.mean(consistencies_values)}')
    print(f'Variance: {np.var(consistencies_values)}')
    print(f'Median: {np.median(consistencies_values)}')
    print(f'Std: {np.std(consistencies_values)}')

    print('RIDGE FREQ')
    # flat_frequency_values = np.concatenate(frequency_values)
    print(f'Mean: {np.mean(frequency_values)}')
    print(f'Variance: {np.var(frequency_values)}')
    print(f'Median: {np.median(frequency_values)}')
    print(f'Std: {np.std(frequency_values)}')

    print('GABOR FILTER')
    # flat_gabor = np.concatenate(gabor_values)
    print(f'Mean: {np.mean(gabor_values)}')
    print(f'Variance: {np.var(gabor_values)}')
    print(f'Median: {np.median(gabor_values)}')
    print(f'Std: {np.std(gabor_values)}')

    headers = ["quality_class", "metric", "mean", "variance", "median", "std_dev", "min", "max", "clarity_mean",
               "clarity_std", "freq_strength_mean", "freq_strength_std"]
    data = []
    data_coherence = {
        'quality_class': quality_class,
        'metric': "Coherence",
        'mean': np.mean(coherences_values),
        'variance': np.var(coherences_values),
        'median': np.median(coherences_values),
        'std_dev': np.std(coherences_values),
        'min': np.min(coherences_values),
        'max': np.max(coherences_values),
        'clarity_mean': np.mean(clarity_means),
        'clarity_std': np.mean(clarity_stds),
        'freq_strength_mean': np.mean(freq_strenghts),
        'freq_strength_std': np.std(freq_strenghts),
    }
    data_orient_const = {
        'quality_class': quality_class,
        'metric': "Orientation Consistency",
        'mean': np.mean(consistencies_values),
        'variance': np.var(consistencies_values),
        'median': np.median(consistencies_values),
        'std_dev': np.std(consistencies_values),
        'min': np.min(consistencies_values),
        'max': np.max(consistencies_values),
        'clarity_mean': "NaN",
        'clarity_std': "NaN",
        'freq_strength_mean': "NaN",
        'freq_strength_std': "NaN"
    }
    data_ridge = {
        'quality_class': quality_class,
        'metric': "Ridge Frequency",
        'mean': np.mean(frequency_values),
        'variance': np.var(frequency_values),
        'median': np.median(frequency_values),
        'std_dev': np.std(frequency_values),
        'min': np.min(frequency_values),
        'max': np.max(frequency_values),
        'clarity_mean': "NaN",
        'clarity_std': "NaN",
        'freq_strength_mean': "NaN",
        'freq_strength_std': "NaN"
    }
    data_gabor = {
        'quality_class': quality_class,
        'metric': "Gabor Filter",
        'mean': np.mean(gabor_values),
        'variance': np.var(gabor_values),
        'median': np.median(gabor_values),
        'std_dev': np.std(gabor_values),
        'min': np.min(gabor_values),
        'max': np.max(gabor_values),
        'clarity_mean': "NaN",
        'clarity_std': "NaN",
        'freq_strength_mean': "NaN",
        'freq_strength_std': "NaN"
    }
    data.append(data_coherence)
    data.append(data_orient_const)
    data.append(data_ridge)
    data.append(data_gabor)

    csv_output_file_path = os.path.join(folder, 'quality_metrics_v3.csv')
    file_exists = os.path.isfile(csv_output_file_path)
    with open(csv_output_file_path, mode='a', newline='', encoding='utf-8') as csv_file:
        print('Saving to csv!')

        writer = csv.DictWriter(csv_file, fieldnames=headers)

        if not file_exists:
            writer.writeheader()

        writer.writerows(data)

        print('Data saved')

    plt.figure(figsize=(10, 6))
    plt.hist(coherences_values, bins=20, color='blue', alpha=0.7)
    plt.title(f'Coherence Distribution for Quality Class: {quality_class}')
    plt.xlabel('Coherence')
    plt.ylabel('Density')
    coherence_plt_name = f'coherence_{quality_class}v3.png'
    plot_file_path = os.path.join(folder, coherence_plt_name)
    plt.savefig(plot_file_path, bbox_inches='tight')
    plt.close()

    plt.figure(figsize=(10, 6))
    plt.hist(consistencies_values, bins=20, color='green', alpha=0.7)
    plt.title(f'Consistency Distribution for Quality Class: {quality_class}')
    plt.xlabel('Consistency')
    plt.ylabel('Density')
    consistency_plt_name = f'consistency_{quality_class}v3.png'
    plot_file_path = os.path.join(folder, consistency_plt_name)
    plt.savefig(plot_file_path, bbox_inches='tight')
    plt.close()

    plt.figure(figsize=(10, 6))
    plt.hist(frequency_values, bins=20, color='red', alpha=0.7)
    plt.title(f'Frequency Distribution for Quality Class: {quality_class}')
    plt.xlabel('Frequency')
    plt.ylabel('Density')
    freq_plt_name = f'freq_{quality_class}v3.png'
    plot_file_path = os.path.join(folder, freq_plt_name)
    plt.savefig(plot_file_path, bbox_inches='tight')
    plt.close()

    plt.figure(figsize=(10, 6))
    plt.hist(gabor_values, bins=20, color='pink', alpha=0.7)
    plt.title(f'Gabor Distribution for Quality Class: {quality_class}')
    plt.xlabel('Gabor')
    plt.ylabel('Density')
    gabor_plt_name = f'gabor_{quality_class}v3.png'
    plot_file_path = os.path.join(folder, gabor_plt_name)
    plt.savefig(plot_file_path, bbox_inches='tight')
    plt.close()

    plt.figure(figsize=(10, 6))
    plt.hist(clarity_means, bins=20, color='black', alpha=0.4)
    plt.title(f'Clarity Means Distribution for Quality Class: {quality_class}')
    plt.xlabel('Clarity Means')
    plt.ylabel('Density')
    clarity_means_plt_name = f'clarity_means_{quality_class}v3.png'
    plot_file_path = os.path.join(folder, clarity_means_plt_name)
    plt.savefig(plot_file_path, bbox_inches='tight')
    plt.close()

    plt.figure(figsize=(10, 6))
    plt.hist(freq_strenghts, bins=20, color='brown', alpha=0.7)
    plt.title(f'Freq Strength Distribution for Quality Class: {quality_class}')
    plt.xlabel('Freq Strength')
    plt.ylabel('Density')
    freq_strength_plt_name = f'freq_strength_{quality_class}v3.png'
    plot_file_path = os.path.join(folder, freq_strength_plt_name)
    plt.savefig(plot_file_path, bbox_inches='tight')
    plt.close()

# consistencies_values = []
# coherences_values = []
# frequency_values = []
# gabor_values = []
#
# for tif_file in tif_files[:100]:
#     print(tif_file)
#
#     print('Reading image')
#     image = utils.read_image(tif_file)
#     # utils.show_image_on_plot(image, 'Original Image')
#
#     print('Normalization')
#     image = utils.normalize_image(image)
#     # utils.show_image_on_plot(image, 'Normalized Image')
#
#     print('Finding Mask')
#     mask = utils.segment_fingerprint(image)
#     # utils.show_image_on_plot(mask, 'Mask fingerprint')
#
#     print('Estimating orientations/coherence')
#     orientations, coherence = np.where(mask == 1.0, img_orientation.estimate_orientation(image, _interpolate=True),
#                                        -1.0)
#
#     coherences_values.append(np.mean(coherence[mask == 1.0]))
#     # print(f'orents: {np.min(orientations)}, {np.max(orientations)}')
#     # print(f'orents: {np.min(coherence)}, {np.max(coherence)}')
#
#     # utils.showOrientations(image, orientations, 'Orientations', 8)  # TODO: gabor filter
#     # plt.show()
#     #
#     # coh_img = plt.imshow(coherence, cmap='jet')
#     # plt.colorbar(coh_img)
#     # plt.title('coherence')
#     # plt.axis('off')
#     # plt.tight_layout()
#     # plt.show()
#
#     # print('Orientation consistency')
#     # orientation_consistency = np.where(mask == 1.0,
#     #                                    img_orientation.measure_orientation_consistency(image, orientations,
#     #                                                                                    _block_size=16), -1.0)
#     orientation_consistency = np.where(mask == 1.0,
#                                        img_orientation.measure_orientation_consistency2(orientations), -1.0)
#
#     consistencies_values.append(np.mean(orientation_consistency[mask == 1.0]))
#
#     # plt.figure(figsize=(15, 5))
#     #
#     # # # Consistency map
#     # # plt.subplot(131)
#     # consistency_map = plt.imshow(orientation_consistency, cmap='jet', vmin=0, vmax=1)
#     # plt.colorbar(consistency_map)
#     # plt.title('Orientation Consistency')
#     # plt.axis('off')
#     #
#     # plt.tight_layout()
#     # plt.show()
#
#     # print(f'const: {np.min(orientation_consistency)}, {np.max(orientation_consistency)}')
#     #
#     # print(
#     #     f'mean: {np.mean(orientation_consistency[orientation_consistency > -1.0])}, '
#     #     f'median:{np.median(orientation_consistency[orientation_consistency > -1.0])}, '
#     #     f'std: {np.std(orientation_consistency[orientation_consistency > -1.0])}')
#
#     # values = fuzzy_orientation_classification.normalize_input_values(coherence[mask == 1],
#     #                                                                  orientation_consistency[mask == 1])
#     #
#     # fuzzy_orientation_classification.fuzzy_classification(values[0], values[1])
#     #
#     # plt.hist(orientation_consistency[orientation_consistency > -1.0].flatten(), bins=20)
#     # plt.title('Histogram of Orientation Consistency')
#     # plt.xlabel('Consistency')
#     # plt.ylabel('Frequency')
#     # plt.show()
#
#     # TODO: for consistency there will be a*mean+b*std
#     # TODO: otsu segmentation
#     # utils.show_image_on_plot(orientation_consistency, 'Orientation consistency')
#
#     print('Estimating frequencies')
#     frequencies = np.where(mask == 1.0, ridge_frequency.estimate_frequencies(image, orientations), -1.0)
#     frequency_values.append(frequencies[mask == 1.0])
#     # imshow = plt.imshow(utils.normalize_image(frequencies), cmap='jet')
#     # plt.colorbar(imshow)
#     # utils.show_image_on_plot(utils.normalize_image(frequencies), 'Frequencies')
#     print(f'freq: {np.min(frequencies[mask == 1.0])}, {np.max(frequencies[mask == 1.0])}')
#     print(f'freq mean: {np.mean(frequencies[mask == 1.0])}, freq std: {np.std(frequencies[mask == 1.0])}')
#     # map, score = fuzzy_ridge_frequency_classification.evaluate_fingerprint_quality(image, frequencies)
#     #
#     # print(
#     #     f'Score: mean: {np.mean(map[mask == 1.0])}, std: {np.std(map[mask == 1.0])}, min: {np.min(map[mask == 1.0])}, max: {np.max(map[mask == 1.0])}')
#
#     # (h, w) = image.shape
#     # for j in range(0, h - 16 + 1, 16):
#     #     for i in range(0, w - 16 + 1, 16):
#     #         img_block = image[j:j + 16, i:i + 16]
#     #         block_freq = frequencies[j + 16 // 2, i + 16 // 2]
#     #         return_vals = fuzzy_ridge_frequency_classification.analyze_frequency_block(img_block, block_freq)
#     #         print(return_vals)
#     # quality_mask = np.full(frequencies.shape, -1)
#     # valid_freq = (frequencies >= (1 / 25)) & (frequencies <= (1 / 3))
#     # quality_mask[valid_freq] = 1
#     # print(f'quality mask: {quality_mask.shape}')
#     # print(f'freq_shape: {frequencies.shape}')
#     # # utils.show_image_on_plot(quality_mask, 'Quality Mask')
#     #
#     print('Filtering')
#     gbr = utils.normalize_image(gabor_filter.apply_gabor_filter(image, orientations, frequencies))
#     print(f'Gabor min: {np.min(gbr[mask == 1.0])} max: {np.max(gbr[mask == 1.0])}')
#     gabor_values.append(gbr[mask == 1.0])
#     gbr = np.where(mask == 1.0, gbr, 1.0)
#     # utils.show_image_on_plot(gbr, "Gabor filter")
#     #
#     # plt.show()
#     #
#
#     # where = np.where(mask == 1.0,
#     #                  img_quality.estimate_quality(orientations, orientation_consistency, frequencies, image), -2.0)
#
#     # print(where[where > -2.0][0])
#
#     # plt.show()
#
# print('COHERENCE')
# print(f'Mean: {np.mean(coherences_values)}')
# print(f'Variance: {np.var(coherences_values)}')
# print(f'Median: {np.median(coherences_values)}')
# print(f'Std: {np.std(coherences_values)}')
#
# print('ORIENTATION CONSISTENCY')
# print(f'Mean: {np.mean(consistencies_values)}')
# print(f'Variance: {np.var(consistencies_values)}')
# print(f'Median: {np.median(consistencies_values)}')
# print(f'Std: {np.std(consistencies_values)}')
#
# print('RIDGE FREQ')
# flat_frequency_values = np.concatenate(frequency_values)
# print(f'Mean: {np.mean(flat_frequency_values)}')
# print(f'Variance: {np.var(flat_frequency_values)}')
# print(f'Median: {np.median(flat_frequency_values)}')
# print(f'Std: {np.std(flat_frequency_values)}')
#
# print('GABOR FILTER')
# flat_gabor = np.concatenate(gabor_values)
# print(f'Mean: {np.mean(flat_gabor)}')
# print(f'Variance: {np.var(flat_gabor)}')
# print(f'Median: {np.median(flat_gabor)}')
# print(f'Std: {np.std(flat_gabor)}')
#
# plt.figure(figsize=(10, 5))
# plt.subplot(1, 4, 1)
# plt.hist(consistencies_values, bins=20, color='blue', alpha=0.7)
# plt.title('Orientation Consistency Distribution')
# plt.xlabel('Consistency Value')
# plt.ylabel('Frequency')
#
# plt.subplot(1, 4, 2)
# plt.hist(coherences_values, bins=20, color='green', alpha=0.7)
# plt.title('Coherence Distribution')
# plt.xlabel('Coherence Value')
# plt.ylabel('Frequency')
#
# plt.subplot(1, 4, 3)
# plt.hist(flat_frequency_values, bins=20, color='green', alpha=0.7)
# plt.title('Ridge frequency')
# plt.xlabel('Freq Value')
# plt.ylabel('Frequency')
#
# plt.subplot(1, 4, 4)
# plt.hist(flat_gabor, bins=20, color='green', alpha=0.7)
# plt.title('Gabor')
# plt.xlabel('Gabor Value')
# plt.ylabel('Frequency')
#
# plt.tight_layout()
# plt.show()
