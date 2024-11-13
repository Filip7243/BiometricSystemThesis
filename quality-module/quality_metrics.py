import os
from pathlib import Path
from typing import Tuple

import cv2
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from scipy import signal, ndimage
from scipy.signal import welch
from scipy.stats import norm
from skimage.feature import graycomatrix, graycoprops, local_binary_pattern

import gabor_filter
import img_orientation
import ridge_frequency
import utils

folder = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\analized_data')
quality_folders = [f for f in folder.iterdir() if f.is_dir()]


def create_folder(folder_path):
    if not os.path.exists(folder_path):
        os.makedirs(folder_path)


clarity_path = os.path.join(folder, 'clarity_plots')
coherence_path = os.path.join(folder, 'coherence_plots')
frequency_path = os.path.join(folder, 'frequency_plots')
gabor_path = os.path.join(folder, 'gabor_plots')

snr_path = os.path.join(folder, 'snr_plots')
cnr_path = os.path.join(folder, 'cnr_plots')
local_noise_path = os.path.join(folder, 'local_noise_plots')

freq_uniformity_path = os.path.join(folder, 'freq_uniformity_plots')
orientation_consistency_path = os.path.join(folder, 'orientation_consistency_plots')

glcm_path = os.path.join(folder, 'glcm_plots')

statistical_properties_path = os.path.join(folder, 'statistical_properties_plots')

global_properties_path = os.path.join(folder, 'global_properties_plots')

clarity_error_path = os.path.join(folder, 'clarity_error_plots')
orientation_error_path = os.path.join(folder, 'orientation_error_plots')
coherence_error_path = os.path.join(folder, 'coherence_error_plots')
frequency_error_path = os.path.join(folder, 'frequency_error_plots')
gabor_error_path = os.path.join(folder, 'gabor_error_plots')

create_folder(clarity_path)
create_folder(coherence_path)
create_folder(frequency_path)
create_folder(gabor_path)

create_folder(snr_path)
create_folder(cnr_path)
create_folder(local_noise_path)

create_folder(freq_uniformity_path)
create_folder(orientation_consistency_path)

create_folder(glcm_path)

create_folder(statistical_properties_path)

create_folder(global_properties_path)

create_folder(clarity_error_path)
create_folder(orientation_error_path)
create_folder(coherence_error_path)
create_folder(frequency_error_path)
create_folder(gabor_error_path)


def save_fig(data, title, x_label, plt_name, folder):
    plt.figure(figsize=(10, 6))
    plt.hist(data, bins=20, color='blue', alpha=0.7)
    plt.title(title)
    plt.xlabel(x_label)
    plt.ylabel('Density')
    plot_file_path = os.path.join(folder, plt_name)
    plt.savefig(plot_file_path, bbox_inches='tight')
    plt.close()


def save_basic_metrics_plots(basic_metric_df, quality_class, save_path):
    for column in basic_metric_df.columns:
        basic_metric = basic_metric_df[column]
        save_fig(basic_metric, f'Clarity distribution of data: {column} in class: {quality_class}',
                 column, f'{quality_class}_clarity_{column}', save_path)
        # TODO: save to csv


def get_basic_metrics(data):
    return {
        'mean': np.mean(data),
        'std': np.std(data),
        'var': np.var(data),
        'median': np.median(data),
        'min': np.min(data),
        'max': np.max(data)
    }


def get_image_clarity(_img, _mask, _block_size=16):
    h, w = _img.shape
    clarity = np.zeros((h // _block_size, w // _block_size))

    for j in range(0, h - _block_size + 1, _block_size):
        for i in range(0, w - _block_size + 1, _block_size):
            if _mask[j:j + _block_size, i:i + _block_size].mean() > 0.6:
                block = _img[j:j + _block_size, i:i + _block_size]
                clarity[j // _block_size, i // _block_size] = np.std(block)

    return clarity


def compute_ridge_count_reliability(
        image: np.ndarray,
        block_size: int = 16,
        min_contrast: int = 30,
        canny_low: int = 50,
        canny_high: int = 150,
        gaussian_kernel_size: int = 3,
        reliability_threshold: float = 0.2
) -> Tuple[np.ndarray, np.ndarray]:
    h, w = image.shape
    blocks_y = h // block_size
    blocks_x = w // block_size

    reliability = np.zeros((blocks_y, blocks_x))
    reliability_mask = np.zeros((blocks_y, blocks_x), dtype=bool)

    img_filtered = cv2.GaussianBlur(
        image,
        (gaussian_kernel_size, gaussian_kernel_size),
        0
    )

    for y in range(blocks_y):
        for x in range(blocks_x):
            y_start = y * block_size
            y_end = y_start + block_size
            x_start = x * block_size
            x_end = x_start + block_size

            block = img_filtered[y_start:y_end, x_start:x_end]

            block_contrast = np.max(block) - np.min(block)
            if block_contrast < min_contrast:
                continue

            edges = cv2.Canny(
                block,
                canny_low,
                canny_high,
                apertureSize=3,
                L2gradient=True
            )

            edge_density = np.sum(edges) / (block_size * block_size)
            reliability[y, x] = edge_density

            if edge_density > reliability_threshold:
                reliability_mask[y, x] = True

    if np.max(reliability) > 0:
        reliability = reliability / np.max(reliability)

    return reliability, reliability_mask


def compute_noise_ratios(_original):
    _filtered = cv2.GaussianBlur(_original, (5, 5), 0)

    signal_power = np.mean(_filtered ** 2)
    noise = _original - _filtered
    noise_power = np.mean(noise ** 2)
    snr = 10 * np.log10(signal_power / noise_power) if noise_power > 0 else 0

    contrast = np.std(_filtered)
    cnr = contrast / np.std(noise) if np.std(noise) > 0 else 0

    noise_est = signal.wiener(_original) - _original
    local_noise = np.mean(abs(noise_est))

    return {
        'snr': snr,
        'cnr': cnr,
        'local_noise': local_noise
    }


def compute_ridge_properties(_ridge_frequency, _orientation):
    freq_uniformity = 1 - np.std(_ridge_frequency[_ridge_frequency > 0]) / np.mean(
        _ridge_frequency[_ridge_frequency > 0])

    orientation_diff = np.abs(np.diff(_orientation, axis=1))
    orientation_consistency = np.mean(np.cos(2 * orientation_diff))

    print('freq_uniformity', freq_uniformity)
    print('orientation_consistency', orientation_consistency)

    return {
        'freq_uniformity': freq_uniformity,
        'orientation_consistency': orientation_consistency
    }


def compute_glcm_features(_img):
    img_scaled = (_img * 255).astype(np.uint8)
    glcm = graycomatrix(img_scaled, [1], [0, np.pi / 4, np.pi / 2, 3 * np.pi / 4], 256, symmetric=True, normed=True)

    features = {
        'glcm_contrast': np.mean(graycoprops(glcm, 'contrast')),
        'glcm_homogeneity': np.mean(graycoprops(glcm, 'homogeneity')),
        'glcm_energy': np.mean(graycoprops(glcm, 'energy')),
        'glcm_correlation': np.mean(graycoprops(glcm, 'correlation')),
        'glcm_dissimilarity': np.mean(graycoprops(glcm, 'dissimilarity'))
    }

    return features


def compute_statistical_properties(_img):
    sobel_x = cv2.Sobel(_img, cv2.CV_64F, 1, 0, ksize=3)
    sobel_y = cv2.Sobel(_img, cv2.CV_64F, 0, 1, ksize=3)
    edge_strength = np.mean(np.sqrt(sobel_x ** 2 + sobel_y ** 2))

    laplacian = cv2.Laplacian(_img, cv2.CV_64F)
    blur_measure = np.var(laplacian)

    hist = np.histogram(_img, bins=256, range=(0, 1))[0]
    hist = hist / np.sum(hist)
    entropy = -np.sum(hist * np.log2(hist + 1e-10))

    result = {
        'edge_strength': edge_strength,
        'blur': blur_measure,
        'entropy': entropy,
        'contrast': np.std(normalized_image),
        'range': np.max(normalized_image) - np.min(normalized_image)
    }

    return result


def compute_global_properties(_img, mask):
    mask_ratio = np.sum(mask[mask == 1.0]) / mask.size

    blurred = cv2.GaussianBlur(_img, (5, 5), 0)
    noise_level = np.mean(np.abs(_img - blurred))

    return {
        'mask_ratio': mask_ratio,
        'noise_level': noise_level
    }


def compute_metric_error(_metrics):
    block_size = 16
    metric_errors = []

    for y in range(0, _metrics.shape[0], block_size):
        for x in range(0, _metrics.shape[1], block_size):
            block_metric = _metrics[y:y + block_size, x:x + block_size]
            block_mean = np.mean(block_metric)
            block_errors = np.abs(block_metric - block_mean)
            metric_errors.extend(block_errors.ravel())

    metric_errors = np.array(metric_errors)
    metric_error_pdf = norm.pdf(metric_errors, loc=0, scale=np.std(metric_errors)).mean()

    print('metric_error_pdf', metric_error_pdf)

    return metric_error_pdf


def compute_shannon_entropy(_normalized_image):
    from skimage.measure import shannon_entropy
    return shannon_entropy(_normalized_image)


def analyze_ridge_frequency(_img):
    fft = np.fft.fft2(_img)
    fft_shifted = np.fft.fftshift(fft)
    magnitude_spectrum = np.abs(fft_shifted)

    # Get the indices of the maximum value in the 2D magnitude spectrum
    max_idx = np.unravel_index(np.argmax(magnitude_spectrum), magnitude_spectrum.shape)

    # Compute the frequencies corresponding to the indices
    frequencies_y = np.fft.fftfreq(_img.shape[0])
    frequencies_x = np.fft.fftfreq(_img.shape[1])

    dominant_freq_y = frequencies_y[max_idx[0]]
    dominant_freq_x = frequencies_x[max_idx[1]]

    return {
        'dominant_frequency_y': dominant_freq_y,
        'dominant_frequency_x': dominant_freq_x,
        'frequency_strength': np.max(magnitude_spectrum) / np.mean(magnitude_spectrum)
    }


def assess_minutiae_quality(_img):
    """Assess quality around minutiae points"""
    # Simple minutiae detection using Harris corner detector
    corners = cv2.cornerHarris(_img, blockSize=2, ksize=3, k=0.04)

    # Analyze local quality around detected points
    threshold = 0.01 * corners.max()
    minutiae_points = (corners > threshold).astype(np.uint8)

    # Calculate local contrast around minutiae
    local_quality = ndimage.gaussian_filter(_img, sigma=2)[minutiae_points > 0]

    return {
        'minutiae_count': np.sum(minutiae_points),
        'minutiae_contrast': np.mean(local_quality) if len(local_quality) > 0 else 0
    }


def compute_freq_energy(_img):
    fft = np.fft.fft2(_img)
    fft_shift = np.fft.fftshift(fft)
    return np.sum(np.abs(fft_shift)[_img.shape[0] // 4:3 * _img.shape[0] // 4,
                  _img.shape[1] // 4:3 * _img.shape[1] // 4])


def compute_background_uniformity(_img):
    """Analyze background region uniformity"""
    # Otsu's thresholding to separate foreground/background
    _, binary = cv2.threshold(_img, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

    # Analyze background regions
    background_mask = binary == 0
    background_pixels = _img[background_mask]

    return {
        'background_variance': np.var(background_pixels) if len(background_pixels) > 0 else 0,
        'background_mean': np.mean(background_pixels) if len(background_pixels) > 0 else 0
    }


def ridge_valley_structure(image):
    # Apply Gabor filter to capture the ridge and valley patterns
    kernels = [cv2.getGaborKernel((21, 21), 4.0, np.pi * theta / 8, 10, 0.5, 0, ktype=cv2.CV_32F)
               for theta in range(8)]

    responses = []
    for kernel in kernels:
        response = cv2.filter2D(image, cv2.CV_32F, kernel)
        responses.append(response)

    # Calculate the energy concentration along the ridges
    energy_map = np.sum([response ** 2 for response in responses], axis=0)

    return energy_map


def orientation_certainty_field(image):
    # Compute the gradient of the image to estimate ridge orientation
    gradient_x = cv2.Sobel(image, cv2.CV_64F, 1, 0, ksize=3)
    gradient_y = cv2.Sobel(image, cv2.CV_64F, 0, 1, ksize=3)

    # Calculate the covariance matrix and eigenvalues to determine orientation consistency
    cov_matrix = np.zeros((2, 2))
    for i in range(image.shape[0]):
        for j in range(image.shape[1]):
            cov_matrix[0, 0] += gradient_x[i, j] ** 2
            cov_matrix[1, 1] += gradient_y[i, j] ** 2
            cov_matrix[0, 1] += gradient_x[i, j] * gradient_y[i, j]

    eigvals = np.linalg.eigvals(cov_matrix)
    eigenvalue_ratio = eigvals[0] / eigvals[1]  # Ratio of eigenvalues
    return eigenvalue_ratio


def pixel_intensity_variance(image):
    # Calculate pixel intensity variance (directional contrast)
    var_image = np.var(image, axis=0)  # Variance along the rows (axis 0)
    return np.mean(var_image)


def global_analysis(image):
    # Smoothness of ridge orientation change across the fingerprint image
    grad_x = cv2.Sobel(image, cv2.CV_64F, 1, 0, ksize=3)
    grad_y = cv2.Sobel(image, cv2.CV_64F, 0, 1, ksize=3)

    orientation_grad = np.sqrt(grad_x ** 2 + grad_y ** 2)
    smoothness_score = np.mean(orientation_grad)

    # Ridge-Valley thickness uniformity (constant ridge-valley thickness ratio)
    ridge_valley_ratio = np.mean(image) / np.std(image)

    return {
        'smoothness_score': smoothness_score,
        'ridge_valley_ratio': ridge_valley_ratio
    }


def lbp_analysis(image, radius=1, n_points=8):
    # Apply LBP to the image
    lbp_image = local_binary_pattern(image, n_points, radius, method='uniform')

    # Return the LBP image
    return lbp_image


# Visualize reliability
# def visualize_reliability(
#         image: np.ndarray,
#         reliability: np.ndarray,
#         reliability_mask: np.ndarray,
#         block_size: int = 16
# ) -> np.ndarray:
#     """
#     Visualize the reliability map overlaid on the original image.
#
#     Parameters:
#     -----------
#     image : np.ndarray
#         Original fingerprint image
#     reliability : np.ndarray
#         Reliability scores from compute_ridge_count_reliability
#     reliability_mask : np.ndarray
#         Binary mask of reliable regions
#     block_size : int
#         Block size used in reliability computation
#
#     Returns:
#     --------
#     np.ndarray
#         Visualization image with reliability overlay
#     """
#     # Create RGB visualization
#     vis_img = cv2.cvtColor(image, cv2.COLOR_GRAY2RGB)
#     h, w = image.shape
#
#     for y in range(reliability.shape[0]):
#         for x in range(reliability.shape[1]):
#             if reliability_mask[y, x]:
#                 y_start = y * block_size
#                 y_end = y_start + block_size
#                 x_start = x * block_size
#                 x_end = x_start + block_size
#
#                 # Overlay reliability score as semi-transparent green
#                 alpha = reliability[y, x]
#                 overlay = vis_img[y_start:y_end, x_start:x_end].copy()
#                 overlay[:, :, 1] = np.clip(overlay[:, :, 1] + 255 * alpha, 0, 255)
#
#                 vis_img[y_start:y_end, x_start:x_end] = overlay
#
#     return vis_img


for quality_folder in quality_folders[:]:
    quality_class = quality_folder.name
    if quality_class == 'v3':
        continue
    files = list(quality_folder.glob('*.png'))

    basic_metrics_clarity = pd.DataFrame()
    basic_metrics_coherence = pd.DataFrame()
    basic_metrics_frequency = pd.DataFrame()
    basic_metrics_gabor = pd.DataFrame()

    snr_values = []
    cnr_values = []
    local_noise_values = []

    freq_uniformity_values = []
    orientation_consistency_values = []

    glcm_features_df = pd.DataFrame()

    statistical_properties_df = pd.DataFrame()

    global_properties_df = pd.DataFrame()

    clarity_error_values = []
    orientation_error_values = []
    coherence_error_values = []
    frequency_error_values = []
    gabor_error_values = []

    shannon_entropy_values = []

    ridge_frequency_metrics_df = pd.DataFrame()

    freq_energy_values = []

    background_properties_df = pd.DataFrame()

    energy_map_means = []

    eigenvalues_ratios = []

    pixel_intensity_values = []

    global_analysis_df = pd.DataFrame()

    lbp_means = []

    relability_means = []

    for file in files[4:6]:
        print(file)

        print('Reading image')
        original = utils.read_image(file)

        background_properties = compute_background_uniformity(original)
        background_properties = pd.DataFrame([background_properties])
        background_properties_df = pd.concat([background_properties_df, background_properties], ignore_index=True)

        print('Computing noises')
        noise_ratios = compute_noise_ratios(original)
        snr_values.append(noise_ratios['snr'])
        cnr_values.append(noise_ratios['cnr'])
        local_noise_values.append(noise_ratios['local_noise'])

        print('Segmenting fingerprint')
        mask = utils.segment_fingerprint(original)

        print('Normalizing image')
        normalized_image = utils.normalize_image(original)

        lbp = lbp_analysis(normalized_image.astype(np.uint8))
        lbp_means.append(np.mean(lbp))

        global_analysis_data = global_analysis(normalized_image)
        global_analysis_data = pd.DataFrame([global_analysis_data])
        global_analysis_df = pd.concat([global_analysis_df, global_analysis_data], ignore_index=True)

        pixel_intensity_variance_value = pixel_intensity_variance(normalized_image)
        pixel_intensity_values.append(pixel_intensity_variance_value)

        eigenvalues_orientation_ratio = orientation_certainty_field(normalized_image)
        eigenvalues_ratios.append(eigenvalues_orientation_ratio)

        energy_map = ridge_valley_structure(normalized_image)
        energy_map_means.append(np.mean(energy_map))

        freq_energy = compute_freq_energy(normalized_image)
        freq_energy_values.append(freq_energy)

        ridge_frequency_metrics = analyze_ridge_frequency(normalized_image)
        ridge_frequency_metrics = pd.DataFrame([ridge_frequency_metrics])
        ridge_frequency_metrics_df = pd.concat([ridge_frequency_metrics_df, ridge_frequency_metrics], ignore_index=True)

        shannon_entropy_values.append(compute_shannon_entropy(normalized_image))

        statistical_properties = compute_statistical_properties(normalized_image)
        statistical_properties = pd.DataFrame([statistical_properties])
        statistical_properties_df = pd.concat([statistical_properties_df, statistical_properties], ignore_index=True)

        global_properties = compute_global_properties(normalized_image, mask)
        global_properties = pd.DataFrame([global_properties])
        global_properties_df = pd.concat([global_properties_df, global_properties], ignore_index=True)

        glcm_features = compute_glcm_features(normalized_image)
        glcm_features = pd.DataFrame([glcm_features])
        glcm_features_df = pd.concat([glcm_features_df, glcm_features], ignore_index=True)

        print('Computing image clarity')
        img_clarity = get_image_clarity(normalized_image, mask)
        image_clarity_basic_metrics = get_basic_metrics(img_clarity)
        image_clarity_basic_metrics = pd.DataFrame([image_clarity_basic_metrics])
        basic_metrics_clarity = pd.concat([basic_metrics_clarity, image_clarity_basic_metrics], ignore_index=True)

        clarity_error = compute_metric_error(img_clarity)
        clarity_error_values.append(clarity_error)

        print('Computing image orientation/coherence')
        orientations, coherence = img_orientation.estimate_orientation(normalized_image, _interpolate=True)
        coherence_basic_metrics = get_basic_metrics(coherence[mask == 1.0])
        coherence_basic_metrics = pd.DataFrame([coherence_basic_metrics])
        basic_metrics_coherence = pd.concat([basic_metrics_coherence, coherence_basic_metrics], ignore_index=True)

        orientation_error = compute_metric_error(orientations)
        orientation_error_values.append(orientation_error)

        coherence_error = compute_metric_error(coherence)
        coherence_error_values.append(coherence_error)

        print('Computing ridge frequency')
        frequencies = ridge_frequency.estimate_frequencies(normalized_image, orientations, _block_size=16)
        frequencies_basic_metrics = get_basic_metrics(frequencies[mask == 1.0])
        frequencies_basic_metrics = pd.DataFrame([frequencies_basic_metrics])
        basic_metrics_frequency = pd.concat([basic_metrics_frequency, frequencies_basic_metrics], ignore_index=True)

        frequency_error = compute_metric_error(frequencies)
        frequency_error_values.append(frequency_error)

        freq_uniformity, orientation_consistency = compute_ridge_properties(frequencies, orientations)

        freq_uniformity_values.append(freq_uniformity)
        orientation_consistency_values.append(orientation_consistency)

        print('Computing Gabor Filter')
        gbr = utils.normalize(gabor_filter.apply_gabor_filter(normalized_image, orientations, frequencies))
        gabor_basic_metrics = get_basic_metrics(gbr[mask == 1.0])
        gabor_basic_metrics = pd.DataFrame([gabor_basic_metrics])
        basic_metrics_gabor = pd.concat([basic_metrics_gabor, gabor_basic_metrics], ignore_index=True)

        gabor_error = compute_metric_error(gbr)
        gabor_error_values.append(gabor_error)

        print('Computing ridge count reliability')
        reliability_map, reliable_regions = compute_ridge_count_reliability(
            original,
            block_size=16,
            min_contrast=30
        )

        resized_mask = cv2.resize(mask, (original.shape[1] // 16, original.shape[0] // 16),
                                  interpolation=cv2.INTER_NEAREST)
        reliability_map_mean = np.mean(reliability_map[resized_mask == 1.0])
        relability_means.append(reliability_map_mean)

        # TODO: std of mean values of basic metrics

    save_basic_metrics_plots(basic_metrics_clarity, quality_class, clarity_path)
    save_basic_metrics_plots(basic_metrics_coherence, quality_class, coherence_path)
    save_basic_metrics_plots(basic_metrics_frequency, quality_class, frequency_path)
    save_basic_metrics_plots(basic_metrics_gabor, quality_class, gabor_path)

    snr_basic_metrics = get_basic_metrics(snr_values)
    snr_basic_metrics = pd.DataFrame([snr_basic_metrics])

    cnr_basic_metrics = get_basic_metrics(cnr_values)
    cnr_basic_metrics = pd.DataFrame([cnr_basic_metrics])

    local_noise_basic_metrics = get_basic_metrics(local_noise_values)
    local_noise_basic_metrics = pd.DataFrame([local_noise_basic_metrics])

    save_basic_metrics_plots(snr_basic_metrics, quality_class, snr_path)
    save_basic_metrics_plots(cnr_basic_metrics, quality_class, cnr_path)
    save_basic_metrics_plots(local_noise_basic_metrics, quality_class, local_noise_path)

    freq_uniformity_basic_metrics = get_basic_metrics(freq_uniformity_values)
    freq_uniformity_basic_metrics = pd.DataFrame([freq_uniformity_basic_metrics])

    orientation_consistency_basic_metrics = get_basic_metrics(orientation_consistency_values)
    orientation_consistency_basic_metrics = pd.DataFrame([orientation_consistency_basic_metrics])

    save_basic_metrics_plots(freq_uniformity_basic_metrics, quality_class, freq_uniformity_path)
    save_basic_metrics_plots(orientation_consistency_basic_metrics, quality_class, orientation_consistency_path)

    # TODO: WHEN in csv add mean values of each metric
    save_basic_metrics_plots(glcm_features_df, quality_class, glcm_path)

    save_basic_metrics_plots(statistical_properties_df, quality_class, statistical_properties_path)

    save_basic_metrics_plots(global_properties_df, quality_class, global_properties_path)

    clarity_error_basic_metrics = get_basic_metrics(clarity_error_values)
    clarity_error_basic_metrics = pd.DataFrame([clarity_error_basic_metrics])

    orientation_error_basic_metrics = get_basic_metrics(orientation_error_values)
    orientation_error_basic_metrics = pd.DataFrame([orientation_error_basic_metrics])

    coherence_error_basic_metrics = get_basic_metrics(coherence_error_values)
    coherence_error_basic_metrics = pd.DataFrame([coherence_error_basic_metrics])

    frequency_error_basic_metrics = get_basic_metrics(frequency_error_values)
    frequency_error_basic_metrics = pd.DataFrame([frequency_error_basic_metrics])

    gabor_error_basic_metrics = get_basic_metrics(gabor_error_values)
    gabor_error_basic_metrics = pd.DataFrame([gabor_error_basic_metrics])

    save_basic_metrics_plots(clarity_error_basic_metrics, quality_class, clarity_error_path)
    save_basic_metrics_plots(orientation_error_basic_metrics, quality_class, orientation_error_path)
    save_basic_metrics_plots(coherence_error_basic_metrics, quality_class, coherence_error_path)
    save_basic_metrics_plots(frequency_error_basic_metrics, quality_class, frequency_error_path)
    save_basic_metrics_plots(gabor_error_basic_metrics, quality_class, gabor_error_path)
