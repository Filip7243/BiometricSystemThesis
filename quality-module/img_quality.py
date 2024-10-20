import numpy as np


def estimate_quality(_orientations, _orientation_consistency, _frequencies, _gabor_response, _block_size=16):
    print(f'cons: {np.min(_orientation_consistency)}, {np.max(_orientation_consistency)}')
    print(f'freqs: {np.min(_frequencies)}, {np.max(_frequencies)}')
    print(f'gabor: {np.min(_gabor_response)}, {np.max(_gabor_response)}')

    y_blocks, x_blocks = _orientations.shape

    local_quality_scores = np.zeros((y_blocks, x_blocks))

    normalized_orientation_consistency = (
            (_orientation_consistency - np.min(_orientation_consistency)) / np.ptp(_orientation_consistency)
    )
    normalized_frequencies = _frequencies - np.min(_frequencies) / np.ptp(_frequencies)
    normalized_gabor_response = (_gabor_response - np.min(_gabor_response)) / np.ptp(_gabor_response)

    print(f'norm cons: {np.min(normalized_orientation_consistency)}, {np.max(normalized_orientation_consistency)}')
    print(f'norm freqs: {np.min(normalized_frequencies)}, {np.max(normalized_frequencies)}')
    print(f'norm gabor: {np.min(normalized_gabor_response)}, {np.max(normalized_gabor_response)}')

    for j in range(y_blocks):
        for i in range(x_blocks):
            local_quality_scores[j, i] = 0.4 * normalized_orientation_consistency[j, i] + \
                                         0.3 * normalized_frequencies[j, i] + \
                                         0.3 * normalized_gabor_response[j, i]

    global_orientation_consistency = np.mean(normalized_orientation_consistency)
    global_frequencies = np.mean(normalized_frequencies)
    global_gabor_response = np.mean(normalized_gabor_response)

    print(f'global_orientation_consistency: {global_orientation_consistency}')
    print(f'global_frequencies: {global_frequencies}')
    print(f'global_gabor_response: {global_gabor_response}')

    global_quality_score = 0.5 * global_orientation_consistency + \
                           0.3 * global_frequencies + \
                           0.2 * global_gabor_response

    print(f'global_quality_score: {global_quality_score}')

    centroid_y, centroid_x = y_blocks // 2, x_blocks // 2  # TODO: try with moments

    print(f'centroid_y: {centroid_y}')
    print(f'centroid_x: {centroid_x}')

    ys = np.arange(y_blocks)[:, np.newaxis]
    xs = np.arange(x_blocks)[np.newaxis, :]
    distances_from_centroid = np.sqrt((ys - centroid_y) ** 2 + (xs - centroid_x) ** 2)

    print(f'distances_from_centroid: {np.min(distances_from_centroid)}, {np.max(distances_from_centroid)}')

    max_distance = np.max(distances_from_centroid)
    block_weights = 1 - (distances_from_centroid / max_distance)

    print(f'block_weights: {np.min(block_weights)}, {np.max(block_weights)}')

    weighted_local_score = np.sum(local_quality_scores * block_weights) / np.sum(block_weights)

    print(f'weighted_local_score: {np.min(weighted_local_score)}, {np.max(weighted_local_score)}')

    final_quality_score = 0.7 * weighted_local_score + 0.3 * global_quality_score

    print(f'final_quality_score: {np.min(final_quality_score)}, {np.max(final_quality_score)}')

    return final_quality_score
