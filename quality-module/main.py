from pathlib import Path

import cv2
import numpy as np
from matplotlib import pyplot as plt

import img_orientation
import utils
import fuzzy_orientation_classification

folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB')
# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB\images\500\png\plain')
tif_files = list(folder.glob('*.tif'))

from fuzzy_freq import evaluate_fingerprint_quality

# from fuzzy_freq import (evaluate_fingerprint_quality,
#                         get_fingerprint_quality_score, visualize_quality_distribution)


# def run_fingerprint_quality_assessment(image_path):
#     img = utils.read_image(image_path)
#     mask = utils.segment_fingerprint(img)
#     orientations, coherence = img_orientation.estimate_orientation(img, _interpolate=True)
#     orientations = np.where(mask == 1.0, orientations, -1.0)
#     frequencies = np.where(mask == 1.0, ridge_frequency.estimate_frequencies(img, orientations), -1.0)
#
#     # 3. Apply fuzzy logic quality assessment
#     quality_map = evaluate_fingerprint_quality(frequencies)
#     overall_quality = get_fingerprint_quality_score(quality_map)
#     quality_stats = visualize_quality_distribution(quality_map)
#
#     # 4. Visualize results
#     plt.figure(figsize=(15, 5))
#
#     # Original image
#     plt.subplot(131)
#     plt.imshow(img, cmap='gray')
#     plt.title('Original Fingerprint')
#     plt.axis('off')
#
#     # Frequency map
#     plt.subplot(132)
#     freq_map = frequencies.copy()
#     freq_map[freq_map < 0] = 0  # Remove negative values for visualization
#     plt.imshow(freq_map, cmap='jet')
#     plt.colorbar(label='Frequency')
#     plt.title('Ridge Frequency Map')
#     plt.axis('off')
#
#     # Quality map
#     plt.subplot(133)
#     plt.imshow(quality_map, cmap='RdYlGn')
#     plt.colorbar(label='Quality Score')
#     plt.title(f'Quality Map\nOverall Score: {overall_quality:.3f}')
#     plt.axis('off')
#
#     plt.tight_layout()
#     plt.show()
#
#     # Print quality statistics
#     print("\nFingerprint Quality Statistics:")
#     print(f"Overall Quality Score: {overall_quality:.3f}")
#     print(f"Min Quality: {quality_stats['min_quality']:.3f}")
#     print(f"Max Quality: {quality_stats['max_quality']:.3f}")
#     print(f"Mean Quality: {quality_stats['mean_quality']:.3f}")
#     print(f"Median Quality: {quality_stats['median_quality']:.3f}")
#     print(f"Quality Standard Deviation: {quality_stats['std_quality']:.3f}")
#     print("\nQuality Percentiles [25%, 50%, 75%]:")
#     print([f"{x:.3f}" for x in quality_stats['quality_percentiles']])


# Example usage
# if __name__ == "__main__":
#     # Make sure you have these packages installed:
#     # pip install numpy matplotlib opencv-python scikit-fuzzy scipy
#
#     # Example with a single image
#     image_path = tif_files[0]
#     img = utils.read_image(image_path)
#     # img = utils.normalize_image(img)  #TODOL
#     mask = utils.segment_fingerprint(img)  #TODO: test the fft_segmentation mask
#
#     utils.show_image_on_plot(mask)
#     orientations, coherence = img_orientation.estimate_orientation(img, _interpolate=True)
#     orientations = np.where(mask == 1.0, orientations, -1.0)
#     frequencies = np.where(mask == 1.0, ridge_frequency.estimate_frequencies(img, orientations), -1.0)
#
#     print(f'frequencies: {np.min(frequencies)}, {np.max(frequencies)}')
#
#     # Get quality assessment
#     quality_map, freq_characteristics = evaluate_fingerprint_quality(img, frequencies)
#
#     # Overall quality score
#     overall_quality = np.mean(quality_map[quality_map > 0])
#     print(f'Overall quality: {overall_quality}')
#     plt.imshow(quality_map, cmap='jet')
#     plt.show()

for tif_file in tif_files:
    print(tif_file)

    print('Reading image')
    image = utils.read_image(tif_file)
    utils.show_image_on_plot(image, 'Original Image')

    print('Normalization')
    image = utils.normalize_image(image)
    # utils.show_image_on_plot(image, 'Normalized Image')

    print('Finding Mask')
    mask = utils.segment_fingerprint(image)
    utils.show_image_on_plot(mask, 'Mask fingerprint')

    print('Estimating orientations/coherence')
    orientations, coherence = np.where(mask == 1.0, img_orientation.estimate_orientation(image, _interpolate=True),
                                       -1.0)
    print(f'orents: {np.min(orientations)}, {np.max(orientations)}')
    print(f'orents: {np.min(coherence)}, {np.max(coherence)}')

    utils.showOrientations(image, orientations, 'Orientations', 8)  # TODO: gabor filter
    plt.show()

    coh_img = plt.imshow(coherence, cmap='jet')
    plt.colorbar(coh_img)
    plt.title('coherence')
    plt.axis('off')
    plt.tight_layout()
    plt.show()

    print('Orientation consistency')
    # orientation_consistency = np.where(mask == 1.0,
    #                                    img_orientation.measure_orientation_consistency(image, orientations,
    #                                                                                    _block_size=16), -1.0)
    orientation_consistency = np.where(mask == 1.0,
                                       img_orientation.measure_orientation_consistency2(orientations), -1.0)

    # plt.figure(figsize=(15, 5))
    #
    # # # Consistency map
    # # plt.subplot(131)
    consistency_map = plt.imshow(orientation_consistency, cmap='jet', vmin=0, vmax=1)
    plt.colorbar(consistency_map)
    plt.title('Orientation Consistency')
    plt.axis('off')

    plt.tight_layout()
    plt.show()

    print(f'const: {np.min(orientation_consistency)}, {np.max(orientation_consistency)}')

    print(
        f'mean: {np.mean(orientation_consistency[orientation_consistency > -1.0])}, '
        f'median:{np.median(orientation_consistency[orientation_consistency > -1.0])}, '
        f'std: {np.std(orientation_consistency[orientation_consistency > -1.0])}')

    values = fuzzy_orientation_classification.normalize_input_values(coherence[coherence > -1.0],
                                                                     orientation_consistency[
                                                                         orientation_consistency > -1.0])

    fuzzy_orientation_classification.fuzzy_classification(values[0], values[1])
    #
    # plt.hist(orientation_consistency[orientation_consistency > -1.0].flatten(), bins=20)
    # plt.title('Histogram of Orientation Consistency')
    # plt.xlabel('Consistency')
    # plt.ylabel('Frequency')
    # plt.show()

    # TODO: for consistency there will be a*mean+b*std
    # TODO: otsu segmentation
    # utils.show_image_on_plot(orientation_consistency, 'Orientation consistency')

    # print('Estimating frequencies')
    # frequencies = np.where(mask == 1.0, ridge_frequency.estimate_frequencies(image, orientations), -1.0)
    # # utils.show_image_on_plot(utils.normalize_image(frequencies), 'Frequencies')
    # print(f'freq: {np.min(frequencies)}, {np.max(frequencies)}')
    # print(f'freq mean: {np.mean(frequencies[frequencies > -1.0])}, freq std: {np.std(frequencies[frequencies > -1.0])}')
    # quality_mask = np.full(frequencies.shape, -1)
    # valid_freq = (frequencies >= (1 / 25)) & (frequencies <= (1 / 3))
    # quality_mask[valid_freq] = 1
    # print(f'quality mask: {quality_mask.shape}')
    # print(f'freq_shape: {frequencies.shape}')
    # # utils.show_image_on_plot(quality_mask, 'Quality Mask')
    #
    # print('Filtering')
    # image = utils.normalize_image(gabor_filter.apply_gabor_filter(image, orientations, frequencies))
    # image = np.where(mask == 1.0, image, 1.0)
    # utils.show_image_on_plot(image, "Gabor filter")

    # where = np.where(mask == 1.0,
    #                  img_quality.estimate_quality(orientations, orientation_consistency, frequencies, image), -2.0)

    # print(where[where > -2.0][0])

    plt.show()
