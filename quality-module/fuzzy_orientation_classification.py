import numpy as np
import skfuzzy as fuzz
from matplotlib import pyplot as plt
from skfuzzy import control as ctrl

LABEL_LOW = 'LOW'
LABEL_MEDIUM = 'MEDIUM'
LABEL_HIGH = 'HIGH'



class FingerprintOrientationClassifier:
    def __init__(self):
        # Input values
        self.coherence = ctrl.Antecedent(np.arange(0, 1.01, 0.01), 'coherence')
        self.consistency = ctrl.Antecedent(np.arange(0, 1.01, 0.01), 'coherence')

        # Output value
        self.quality = ctrl.Consequent(np.arange(0, 1.01, 0.01), 'Quality')






# TODO: add weights based on distance to center! and create class like on ridge freq
def normalize_input_values(_coherences, _orientations_consistency):
    min_coherence = np.min(_coherences)
    max_coherence = np.max(_coherences)

    min_orientation_consistency = np.min(_orientations_consistency)
    max_orientation_consistency = np.max(_orientations_consistency)

    normalized_coherences = (_coherences - min_coherence) / (max_coherence - min_coherence)
    normalized_orientations_consistency = (_orientations_consistency - min_orientation_consistency) / (
            max_orientation_consistency - min_orientation_consistency)

    print(f'Normalized coherences: {np.min(normalized_coherences)}, {np.max(normalized_coherences)}')
    print(
        f'normalized_orientations_consistency: {np.min(normalized_orientations_consistency)}, '
        f'{np.max(normalized_orientations_consistency)}'
    )

    coherences_mean = np.mean(_coherences)
    coherences_std = np.std(_coherences)
    coherences_median = np.median(_coherences)

    consistency_mean = np.mean(_orientations_consistency)
    consistency_std = np.std(_orientations_consistency)
    consistency_median = np.median(_orientations_consistency)

    weights = {
        'mean': 0.7,
        'std': 0.2,
        'median': 0.1
    }

    print(f'Coherence mean: {coherences_mean}')
    print(f'Coherence std: {coherences_std}')
    print(f'Coherence median: {coherences_median}')

    combined_coherence = (weights['mean'] * coherences_mean +
                          weights['std'] * coherences_std +
                          weights['median'] * coherences_median)

    print(f'Combined coherences: {combined_coherence}')

    print(f'Consistency mean: {consistency_mean}')
    print(f'Consistency std: {consistency_std}')
    print(f'Consistency median: {consistency_median}')

    combined_consistency = (weights['mean'] * consistency_mean +
                            weights['std'] * consistency_std +
                            weights['median'] * consistency_median)

    print(f'Combined consistency: {combined_consistency}')

    return combined_coherence, combined_consistency


def fuzzy_classification(_coherence_score, _consistency_score):
    # Input values
    coherence = ctrl.Antecedent(np.arange(0, 1.01, 0.01), 'Coherence')
    consistency = ctrl.Antecedent(np.arange(0, 1.01, 0.01), 'Consistency')

    # Output value
    quality = ctrl.Consequent(np.arange(0, 1.01, 0.01), 'Quality')

    # TODO: to improve the fuzzy sets, maybe gaus maybe other, and the segmentaion otsu would be better
    # Fuzzy set for coherence
    coherence[LABEL_LOW] = fuzz.trapmf(coherence.universe, [0, 0, 0.4, 0.6])
    coherence[LABEL_MEDIUM] = fuzz.trapmf(coherence.universe, [0.4, 0.5, 0.7, 0.8])
    coherence[LABEL_HIGH] = fuzz.trapmf(coherence.universe, [0.7, 0.8, 1.0, 1.0])

    # Fuzzy set for consistency
    consistency[LABEL_LOW] = fuzz.trapmf(consistency.universe, [0, 0, 0.4, 0.6])
    consistency[LABEL_MEDIUM] = fuzz.trapmf(consistency.universe, [0.4, 0.5, 0.7, 0.8])
    consistency[LABEL_HIGH] = fuzz.trapmf(consistency.universe, [0.7, 0.8, 1.0, 1.0])

    # Fuzzy set for quality
    quality[LABEL_LOW] = fuzz.trimf(quality.universe, [0, 0, 0.5])
    quality[LABEL_MEDIUM] = fuzz.trimf(quality.universe, [0, 0.5, 1.0])
    quality[LABEL_HIGH] = fuzz.trimf(quality.universe, [0.5, 1.0, 1.0])

    # Rules
    rules = create_rules(coherence, consistency, quality)

    quality_control = ctrl.ControlSystem(rules)
    quality_simulation = ctrl.ControlSystemSimulation(quality_control)

    quality_simulation.input['Coherence'] = _coherence_score
    quality_simulation.input['Consistency'] = _consistency_score

    quality_simulation.compute()

    quality_score = quality_simulation.output['Quality']
    print(f'Quality score: {quality_score}')

    coherence.view()
    consistency.view()
    quality.view()
    quality.view(sim=quality_simulation)
    plt.show()


def create_rules(coherence, consistency, quality):
    rule1 = ctrl.Rule(coherence[LABEL_LOW] | consistency[LABEL_LOW], quality[LABEL_LOW])
    rule2 = ctrl.Rule(coherence[LABEL_MEDIUM] & consistency[LABEL_MEDIUM], quality[LABEL_MEDIUM])
    rule3 = ctrl.Rule(coherence[LABEL_HIGH] | consistency[LABEL_HIGH], quality[LABEL_HIGH])

    return [rule1, rule2, rule3]
