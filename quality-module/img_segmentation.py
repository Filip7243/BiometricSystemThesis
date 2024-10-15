import numpy as np
from scipy.ndimage import rotate


def compute_orientation_weight(image, k):
    """
    Computes the orientation weights p(0), p(60), and p(120) for a fingerprint image.

    Parameters:
    - image: 2D numpy array representing the grayscale fingerprint image.
    - k: size of the filter mask (neighborhood) to compute the weights.

    Returns:
    - mean_weight: A 2D numpy array representing the mean of the orientation weights.
    """

    # Define a function to compute the horizontal weight (p(0)) in the image
    def compute_weight_0(image, k):
        rows, cols = image.shape
        weight_0 = np.zeros_like(image)

        for y in range(rows - k):
            for x in range(cols - k):
                neighborhood = image[y:y + k, x:x + k]
                weight = np.sum(np.abs(neighborhood[:-1, :] - neighborhood[1:, :]))
                weight_0[y, x] = weight
        return weight_0

    # Compute the weight for 0 degrees
    weight_0 = compute_weight_0(image, k)

    # Rotate the image by 60 and 120 degrees to compute p(60) and p(120)
    weight_60 = compute_weight_0(rotate(image, 60, reshape=False), k)
    weight_120 = compute_weight_0(rotate(image, 120, reshape=False), k)

    # Rotate the weights back to the original orientation
    weight_60 = rotate(weight_60, -60, reshape=False)
    weight_120 = rotate(weight_120, -120, reshape=False)

    # Compute the mean weight at each pixel
    mean_weight = (weight_0 + weight_60 + weight_120) / 3

    return mean_weight


def create_fingerprint_mask(image, k, threshold=800):
    """
    Creates a binary mask for a fingerprint image using orientation-based weights.

    Parameters:
    - image: 2D numpy array representing the grayscale fingerprint image.
    - k: size of the filter mask (neighborhood) to compute the weights.
    - threshold: the threshold value to separate fingerprint from background (default is 800).

    Returns:
    - mask: A binary mask where 1 represents the fingerprint and 0 represents the background.
    """
    # Compute the orientation weights and the mean weight
    mean_weight = compute_orientation_weight(image, k)

    print(mean_weight[mean_weight > threshold])

    # Apply the threshold to generate the mask
    mask = np.where(mean_weight > threshold, 1, 0)

    return mask
