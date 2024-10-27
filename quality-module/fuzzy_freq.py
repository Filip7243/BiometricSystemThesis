import matplotlib.pyplot as plt
import numpy as np
import skfuzzy as fuzz
from skfuzzy import control as ctrl


class AdvancedFingerprintFuzzySystem:
    def __init__(self, min_wave_length=5, max_wave_length=15):
        # Input variables - multiple frequency characteristics
        freq_range = np.linspace(0, 1 / min_wave_length * 1.2, 100)
        self.frequency = ctrl.Antecedent(freq_range, 'frequency')
        self.freq_consistency = ctrl.Antecedent(np.linspace(0, 1, 100), 'freq_consistency')
        self.freq_strength = ctrl.Antecedent(np.linspace(0, 1, 100), 'freq_strength')
        self.local_contrast = ctrl.Antecedent(np.linspace(0, 1, 100), 'local_contrast')

        # Output variable with more granular levels
        self.quality = ctrl.Consequent(np.linspace(0, 1, 100), 'quality')

        # Calculate optimal frequency parameters
        self.optimal_freq = 1 / ((max_wave_length + min_wave_length) / 2)
        self.freq_range = 1 / min_wave_length - 1 / max_wave_length

        # Membership functions for frequency (using gaussian combination)
        self.frequency['very_low'] = fuzz.gaussmf(self.frequency.universe, 0, 0.02)
        self.frequency['low'] = fuzz.gaussmf(self.frequency.universe, 1 / max_wave_length, 0.02)
        self.frequency['optimal'] = fuzz.gaussmf(self.frequency.universe, self.optimal_freq, 0.02)
        self.frequency['high'] = fuzz.gaussmf(self.frequency.universe, 1 / min_wave_length, 0.02)
        self.frequency['very_high'] = fuzz.gaussmf(self.frequency.universe, 1 / min_wave_length * 1.2, 0.02)

        # Membership functions for frequency consistency
        self.freq_consistency['low'] = fuzz.trapmf(self.freq_consistency.universe, [0, 0, 0.3, 0.4])
        self.freq_consistency['medium'] = fuzz.trapmf(self.freq_consistency.universe, [0.3, 0.4, 0.6, 0.7])
        self.freq_consistency['high'] = fuzz.trapmf(self.freq_consistency.universe, [0.6, 0.7, 1, 1])

        # Membership functions for frequency strength
        self.freq_strength['weak'] = fuzz.trapmf(self.freq_strength.universe, [0, 0, 0.3, 0.4])
        self.freq_strength['moderate'] = fuzz.trapmf(self.freq_strength.universe, [0.3, 0.4, 0.6, 0.7])
        self.freq_strength['strong'] = fuzz.trapmf(self.freq_strength.universe, [0.6, 0.7, 1, 1])

        # Membership functions for local contrast
        self.local_contrast['low'] = fuzz.trapmf(self.local_contrast.universe, [0, 0, 0.3, 0.4])
        self.local_contrast['medium'] = fuzz.trapmf(self.local_contrast.universe, [0.3, 0.4, 0.6, 0.7])
        self.local_contrast['high'] = fuzz.trapmf(self.local_contrast.universe, [0.6, 0.7, 1, 1])

        # Membership functions for quality (using more granular gaussian combination)
        self.quality['very_poor'] = fuzz.gaussmf(self.quality.universe, 0.1, 0.1)
        self.quality['poor'] = fuzz.gaussmf(self.quality.universe, 0.3, 0.1)
        self.quality['fair'] = fuzz.gaussmf(self.quality.universe, 0.5, 0.1)
        self.quality['good'] = fuzz.gaussmf(self.quality.universe, 0.7, 0.1)
        self.quality['excellent'] = fuzz.gaussmf(self.quality.universe, 0.9, 0.1)

        # Enhanced rule base
        self.rules = self._create_rule_base()

        # Create control system
        self.quality_ctrl = ctrl.ControlSystem(self.rules)
        self.quality_simulator = ctrl.ControlSystemSimulation(self.quality_ctrl)

    def _create_rule_base(self):
        """Create comprehensive rule base for fuzzy inference"""
        rules = [
            # Optimal frequency cases
            ctrl.Rule(
                self.frequency['optimal'] & self.freq_consistency['high'] &
                self.freq_strength['strong'] & self.local_contrast['high'],
                self.quality['excellent']
            ),

            # Good frequency cases with varying other parameters
            ctrl.Rule(
                self.frequency['optimal'] & self.freq_consistency['medium'] &
                self.freq_strength['moderate'],
                self.quality['good']
            ),

            # Fair cases
            ctrl.Rule(
                (self.frequency['low'] | self.frequency['high']) &
                self.freq_consistency['medium'],
                self.quality['fair']
            ),

            # Poor cases
            ctrl.Rule(
                (self.frequency['very_low'] | self.frequency['very_high']) &
                self.freq_consistency['low'],
                self.quality['poor']
            ),

            # Very poor cases
            ctrl.Rule(
                (self.frequency['very_low'] | self.frequency['very_high']) &
                self.freq_strength['weak'] & self.local_contrast['low'],
                self.quality['very_poor']
            ),

            # Additional complex rules
            ctrl.Rule(
                self.frequency['optimal'] & self.freq_consistency['high'] &
                self.freq_strength['moderate'] & self.local_contrast['medium'],
                self.quality['good']
            ),

            ctrl.Rule(
                (self.frequency['low'] | self.frequency['high']) &
                self.freq_consistency['high'] & self.freq_strength['strong'],
                self.quality['good']
            )
        ]
        return rules

    def evaluate_block_quality(self, freq_data):
        """
        Evaluate block quality using multiple frequency characteristics

        Args:
            freq_data: Dictionary containing frequency analysis data

        Returns:
            float: Quality score between 0 and 1
        """
        if freq_data['frequency'] <= 0:
            return 0.0

        try:
            self.quality_simulator.input['frequency'] = freq_data['frequency']
            self.quality_simulator.input['freq_consistency'] = freq_data['consistency']
            self.quality_simulator.input['freq_strength'] = freq_data['strength']
            self.quality_simulator.input['local_contrast'] = freq_data['contrast']

            self.quality_simulator.compute()
            return self.quality_simulator.output['quality']
        except:
            return 0.0


def analyze_frequency_block(img_block, frequency):
    """
    Analyze frequency characteristics of an image block

    Args:
        img_block: Image block to analyze
        frequency: Estimated ridge frequency for the block

    Returns:
        dict: Frequency analysis data
    """
    if frequency <= 0:
        return {
            'frequency': 0,
            'consistency': 0,
            'strength': 0,
            'contrast': 0
        }

    # Calculate local contrast
    print(f'img_block: {np.min(img_block)}, {np.max(img_block)}')
    contrast = np.std(img_block)  # Normalize by half of max intensity

    # Calculate frequency strength using FFT
    fft = np.fft.fft2(img_block)
    fft_mag = np.abs(np.fft.fftshift(fft))
    freq_strength = np.max(fft_mag) / np.mean(fft_mag)
    freq_strength = np.clip(freq_strength / 100.0, 0, 1)  # Normalize and clip

    # Calculate frequency consistency
    # Using coefficient of variation of local frequencies
    local_freqs = []
    for i in range(0, img_block.shape[0] - 8, 4):
        for j in range(0, img_block.shape[1] - 8, 4):
            sub_block = img_block[i:i + 8, j:j + 8]
            print(f'Sub blcok std: {np.std(sub_block)}')
            if np.std(sub_block) > 0.05:  # Skip uniform regions
                local_freqs.append(np.mean(np.abs(np.diff(sub_block, axis=0))))

    if local_freqs:
        print("!DUPA!")
        freq_consistency = 1 - np.std(local_freqs) / np.mean(local_freqs)
        freq_consistency = np.clip(freq_consistency, 0, 1)
    else:
        freq_consistency = 0

    return {
        'frequency': frequency,
        'consistency': freq_consistency,
        'strength': freq_strength,
        'contrast': contrast
    }


def evaluate_fingerprint_quality(img, frequencies, block_size=32):
    """
    Evaluate fingerprint quality using advanced frequency analysis

    Args:
        img: Original fingerprint image
        frequencies: Ridge frequencies from estimate_frequencies()
        block_size: Size of blocks for analysis

    Returns:
        tuple: (quality_map, frequency_characteristics)
    """
    fuzzy_system = AdvancedFingerprintFuzzySystem()

    h, w = frequencies.shape
    quality_map = np.zeros_like(frequencies)
    freq_characteristics = np.zeros((h, w, 4))  # Store all characteristics

    for j in range(0, h - block_size + 1, block_size):
        for i in range(0, w - block_size + 1, block_size):
            img_block = img[j:j + block_size, i:i + block_size]
            block_freq = frequencies[j + block_size // 2, i + block_size // 2]

            # Analyze frequency characteristics
            freq_data = analyze_frequency_block(img_block, block_freq)

            # Store characteristics
            freq_characteristics[j:j + block_size, i:i + block_size] = [
                freq_data['frequency'],
                freq_data['consistency'],
                freq_data['strength'],
                freq_data['contrast']
            ]

            # Calculate quality
            quality = fuzzy_system.evaluate_block_quality(freq_data)
            quality_map[j:j + block_size, i:i + block_size] = quality

    consistency_map = freq_characteristics[:, :, 3]
    imshow = plt.imshow(consistency_map, cmap='viridis', interpolation='nearest')
    plt.colorbar(imshow)
    plt.show()

    return quality_map, freq_characteristics
