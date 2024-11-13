from pathlib import Path

import cv2
import imageio.v3
import numpy as np
from matplotlib import pyplot as plt
from scipy import signal

import calude_snr_test as csnr

import claude_test5 as ct
import gabor_filter
import img_orientation
import ridge_frequency
import utils

folder = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\analized_data')
quality_folders = [f for f in folder.iterdir() if f.is_dir()]

quality_system = ct.OptimizedFingerprintQuality(block_size=16)

for quality_folder in quality_folders[5:]:
    quality_class = quality_folder.name
    files = list(quality_folder.glob('*.png'))

    for file in files[:10]:
        print(file)
        image = utils.read_image(
            file)

        mask = utils.segment_fingerprint(image)
        image = utils.normalize_image(image)
        # orientations, coherence = np.where(mask == 1.0,
        #                                    img_orientation.estimate_orientation(
        #                                        image,
        #                                        _interpolate=True),
        #                                    -1.0)
        orientations, coh = img_orientation.estimate_orientation(image, _interpolate=True)
        print(
            f'orient mean: {np.mean(orientations[mask == 1.0])} - {np.min(orientations[mask == 1.0])}, {np.max(orientations[mask == 1.0])}')
        print(f'coh mean: {np.mean(coh[mask == 1.0])} - {np.min(coh[mask == 1.0])}, {np.max(coh[mask == 1.0])}')

        utils.showOrientations(image, orientations, 'orient', _block_size=16)
        plt.show()
        # frequencies = np.where(mask == 1.0, ridge_frequency.estimate_frequencies(image, orientations, 16), -1.0)
        frequencies = ridge_frequency.estimate_frequencies(image, orientations, 16)

        gbr = utils.normalize(gabor_filter.apply_gabor_filter(image, orientations, frequencies))

        quality_metrics = quality_system.evaluate_quality(
            img=image,
            mask=mask,
            orientations=orientations,
            frequencies=frequencies,
            filtered_img=gbr
        )

        # quality_system.visualize_quality(image, quality_metrics)

        print(f"Overall quality score: {quality_metrics.quality_score:.3f}")
        print(f"SNR: {quality_metrics.snr:.2f} dB")
