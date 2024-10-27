import numpy as np
import skfuzzy as fuzz
from matplotlib import pyplot as plt
from skfuzzy import control as ctrl

LABEL_VERY_LOW = 'VERY_LOW'
LABEL_LOW = 'LOW'
LABEL_OPTIMAL = 'OPTIMAL'
LABEL_HIGH = 'HIGH'
LABEL_VERY_HIGH = 'VERY_HIGH'

LABEL_MEDIUM = 'MEDIUM'

LABEL_WEAK = 'WEAK'
LABEL_MODERATE = 'MODERATE'
LABEL_STRONG = 'STRONG'

LABEL_VERY_POOR = 'VERY_POOR'
LABEL_POOR = 'POOR'
LABEL_FAIR = 'FAIR'
LABEL_GOOD = 'GOOD'
LABEL_EXCELLENT = 'EXCELLENT'


class FingerprintRidgeClassifier:
    def __init__(self, min_wave_length=5, max_wave_length=15):
        # Input values
        freq_range = np.linspace(0, 1 / min_wave_length * 1.2, 100)
        self.frequency = ctrl.Antecedent(freq_range, 'Frequency')
        self.frequency_consistency = ctrl.Antecedent(np.arange(0, 1.01, 0.01), 'Frequency_Consistency')
        self.frequency_strength = ctrl.Antecedent(np.arange(0, 1.01, 0.01), 'Frequency_Strength')
        self.local_contrast = ctrl.Antecedent(np.arange(0, 1.01, 0.01), 'Local_Contrast')

        # Output value
        self.quality = ctrl.Consequent(np.arange(0, 1.01, 0.01), 'Quality')

        self.optimal_frequency = 1 / ((max_wave_length + min_wave_length) / 2)
        self.frequency_range = (1 / min_wave_length) - (1 / max_wave_length)

        self.frequency[LABEL_VERY_LOW] = fuzz.gaussmf(self.frequency.universe, 0, 0.02)
        self.frequency[LABEL_LOW] = fuzz.gaussmf(self.frequency.universe, 1 / max_wave_length, 0.02)
        self.frequency[LABEL_OPTIMAL] = fuzz.gaussmf(self.frequency.universe, self.optimal_frequency, 0.02)
        self.frequency[LABEL_HIGH] = fuzz.gaussmf(self.frequency.universe, 1 / min_wave_length, 0.02)
        self.frequency[LABEL_VERY_HIGH] = fuzz.gaussmf(self.frequency.universe, 1 / min_wave_length * 1.2, 0.02)

        self.frequency_consistency[LABEL_LOW] = fuzz.trapmf(self.frequency_consistency.universe, [0, 0, 0.3, 0.4])
        self.frequency_consistency[LABEL_MEDIUM] = fuzz.trapmf(self.frequency_consistency.universe,
                                                               [0.3, 0.4, 0.6, 0.7])
        self.frequency_consistency[LABEL_HIGH] = fuzz.trapmf(self.frequency_consistency.universe, [0.6, 0.7, 1, 1])

        self.frequency_strength[LABEL_WEAK] = fuzz.trapmf(self.frequency_strength.universe, [0, 0, 0.3, 0.4])
        self.frequency_strength[LABEL_MODERATE] = fuzz.trapmf(self.frequency_strength.universe, [0.3, 0.4, 0.6, 0.7])
        self.frequency_strength[LABEL_STRONG] = fuzz.trapmf(self.frequency_strength.universe, [0.6, 0.7, 1, 1])

        self.local_contrast[LABEL_LOW] = fuzz.trapmf(self.local_contrast.universe, [0, 0, 0.3, 0.4])
        self.local_contrast[LABEL_MEDIUM] = fuzz.trapmf(self.local_contrast.universe, [0.3, 0.4, 0.6, 0.7])
        self.local_contrast[LABEL_HIGH] = fuzz.trapmf(self.local_contrast.universe, [0.6, 0.7, 1, 1])

        self.quality[LABEL_VERY_POOR] = fuzz.gaussmf(self.quality.universe, 0.1, 0.1)
        self.quality[LABEL_POOR] = fuzz.gaussmf(self.quality.universe, 0.3, 0.1)
        self.quality[LABEL_FAIR] = fuzz.gaussmf(self.quality.universe, 0.5, 0.1)
        self.quality[LABEL_GOOD] = fuzz.gaussmf(self.quality.universe, 0.7, 0.1)
        self.quality[LABEL_EXCELLENT] = fuzz.gaussmf(self.quality.universe, 0.9, 0.1)

        rules = [
            # Optimal frequency cases
            ctrl.Rule(
                self.frequency[LABEL_OPTIMAL] & self.frequency_consistency[LABEL_HIGH] &
                self.frequency_strength[LABEL_STRONG] & self.local_contrast[LABEL_HIGH],
                self.quality[LABEL_EXCELLENT]
            ),

            # Good frequency cases with varying other parameters
            ctrl.Rule(
                self.frequency[LABEL_OPTIMAL] & self.frequency_consistency[LABEL_MEDIUM] &
                self.frequency_strength[LABEL_MODERATE],
                self.quality[LABEL_GOOD]
            ),

            # Fair cases
            ctrl.Rule(
                (self.frequency[LABEL_LOW] | self.frequency[LABEL_HIGH]) & self.frequency_consistency[LABEL_MEDIUM],
                self.quality[LABEL_FAIR]
            ),

            # Poor cases
            ctrl.Rule(
                (self.frequency[LABEL_VERY_LOW] | self.frequency[LABEL_VERY_HIGH]) &
                self.frequency_consistency[LABEL_LOW],
                self.quality[LABEL_POOR]
            ),

            # Very poor cases
            ctrl.Rule(
                (self.frequency[LABEL_VERY_LOW] | self.frequency[LABEL_VERY_HIGH]) &
                self.frequency_strength[LABEL_WEAK] & self.local_contrast[LABEL_LOW],
                self.quality[LABEL_VERY_POOR]
            ),

            # Additional complex rules
            ctrl.Rule(
                self.frequency[LABEL_OPTIMAL] & self.frequency_consistency[LABEL_HIGH] &
                self.frequency_strength[LABEL_MODERATE] & self.local_contrast[LABEL_MEDIUM],
                self.quality[LABEL_GOOD]
            ),

            ctrl.Rule(
                (self.frequency[LABEL_LOW] | self.frequency[LABEL_HIGH]) &
                self.frequency_consistency[LABEL_HIGH] & self.frequency_strength[LABEL_STRONG],
                self.quality[LABEL_GOOD]
            )
        ]

        self.quality_ctrl = ctrl.ControlSystem(rules)
        self.quality_simulator = ctrl.ControlSystemSimulation(self.quality_ctrl)

    def evaluate_block_quality(self, _frequency_data):
        if _frequency_data['frequency'] <= 0:
            return 0.0

        try:
            self.quality_simulator.input['Frequency'] = _frequency_data['frequency']
            self.quality_simulator.input['Frequency_Consistency'] = _frequency_data['consistency']
            self.quality_simulator.input['Frequency_Strength'] = _frequency_data['strength']
            self.quality_simulator.input['Local_Contrast'] = _frequency_data['contrast']

            self.quality_simulator.compute()
            return self.quality_simulator.output['Quality']
        except:
            return 0.0


def analyze_frequency_block(_img_block, _frequency_block):
    if _frequency_block <= 0:
        print('Frequency block is negative, invalid!')
        return {
            'frequency': 0,
            'consistency': 0,
            'strength': 0,
            'contrast': 0
        }

    # Contrast of img_block
    contrast = np.std(_img_block)
    print(f'Block contrast: {contrast}')

    # Strength of ridges
    fft = np.fft.fft2(_img_block)
    fft_mag = np.abs(np.fft.fftshift(fft))
    freq_strength = np.max(fft_mag) / np.mean(fft_mag)
    print(f'Frequency strength1: {freq_strength}')
    freq_strength = np.clip(freq_strength / 100.0, 0, 1)  # Normalize and clip
    print(
        f'Frequency strength2: {freq_strength}')  # TODO: zoabcyzc to w czacie i ogarnac do konca ten kod z fuzzy_freq i dodac gabor i essunia, pozniej poekperymentowac z innymi funkcjami przynalzeznosci i na sam koneic pobawic sie otsu segmentacja oraz dodac jeszcze klasyfikacje na podstawie cech fizycznych orbzu, czyli stosunek ile pacla do calego tla itd.

    (h, w) = _img_block.shape
    sub_block_size = 8
    contrast_threshold = 0.05
    local_frequencies = []
    for j in range(0, h - sub_block_size, sub_block_size // 2):
        for i in range(0, w - sub_block_size, sub_block_size // 2):
            sub_block = _img_block[j: j + sub_block_size, i: i + sub_block_size]
            if np.std(sub_block) > contrast_threshold:  # Skip blocks with too low contrast
                local_frequencies.append(np.mean(np.abs(np.diff(sub_block, axis=0))))

    if local_frequencies:
        freq_consistency = 1 - np.std(local_frequencies) / np.mean(local_frequencies)
        freq_consistency = np.clip(freq_consistency, 0, 1)
    else:
        freq_consistency = 0

    return {
        'frequency': _frequency_block,
        'consistency': freq_consistency,
        'strength': freq_strength,
        'contrast': contrast
    }


def evaluate_fingerprint_quality(_img, _frequencies, _block_size=32):
    classifier = FingerprintRidgeClassifier()

    (h, w) = _frequencies.shape
    quality_map = np.zeros_like(_frequencies)
    frequency_scores = np.zeros((h, w, 4))

    for j in range(0, h - _block_size + 1, _block_size):
        for i in range(0, w - _block_size + 1, _block_size):
            img_block = _img[j: j + _block_size, i: i + _block_size]
            frequency_block = _frequencies[
                j + _block_size // 2, i + _block_size // 2]  # TODO: maybe not the center, maybe mean

            frequency_data = analyze_frequency_block(img_block, frequency_block)

            frequency_scores[j: j + _block_size, i: i + _block_size] = [
                frequency_data['frequency'],
                frequency_data['consistency'],
                frequency_data['strength'],
                frequency_data['contrast']
            ]

            quality = classifier.evaluate_block_quality(frequency_data)
            quality_map[j: j + _block_size, i: i + _block_size] = quality

    consistency_map = frequency_scores[:, :, 3]
    imshow = plt.imshow(quality_map, cmap='viridis', interpolation='nearest')
    plt.colorbar(imshow)
    plt.show()

    return quality_map, frequency_scores
