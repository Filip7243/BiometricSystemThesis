import numpy as np
from skimage.feature import graycomatrix, graycoprops
import cv2


def extract_glcm_features(image, distances=[1], angles=[0, np.pi / 4, np.pi / 2, 3 * np.pi / 4]):
    """
    Extract GLCM features from a fingerprint image.

    Parameters:
    image: numpy array
        Input fingerprint image
    distances: list of int
        Distance offsets for GLCM computation
    angles: list of float
        Angles for GLCM computation

    Returns:
    dict: Dictionary containing GLCM features
    """
    # Convert to grayscale if needed
    if len(image.shape) > 2:
        image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Normalize image to 8 levels to reduce computation time and noise
    bins = 8
    image = np.uint8(np.floor(image / (256 / bins)))

    # Calculate GLCM
    glcm = graycomatrix(image,
                        distances=distances,
                        angles=angles,
                        levels=bins,
                        symmetric=True,
                        normed=True)

    # Calculate GLCM properties
    features = {}

    # Contrast - Measure of local intensity variation
    features['contrast'] = graycoprops(glcm, 'contrast').mean()

    # Homogeneity - Measures closeness of element distribution
    features['homogeneity'] = graycoprops(glcm, 'homogeneity').mean()

    # Energy - Measures uniformity of texture
    features['energy'] = graycoprops(glcm, 'energy').mean()

    # Correlation - Measures linear dependency of gray levels
    features['correlation'] = graycoprops(glcm, 'correlation').mean()

    # Dissimilarity - Similar to contrast but with linear weighting
    features['dissimilarity'] = graycoprops(glcm, 'dissimilarity').mean()

    # Calculate entropy manually as it's not in graycoprops
    eps = 1e-10  # Small constant to avoid log(0)
    entropy = -np.sum(glcm * np.log2(glcm + eps), axis=(2, 3)).mean()
    features['entropy'] = entropy

    return features


def analyze_fingerprint_quality(image):
    """
    Analyze fingerprint quality using GLCM features.

    Parameters:
    image: numpy array
        Input fingerprint image

    Returns:
    tuple: (quality_score, feature_analysis)
    """
    features = extract_glcm_features(image)

    # Define feature weights based on importance
    weights = {
        'contrast': 0.2,
        'homogeneity': 0.2,
        'energy': 0.15,
        'correlation': 0.15,
        'dissimilarity': 0.15,
        'entropy': 0.15
    }

    # Analysis of each feature
    analysis = {
        'contrast': 'High' if features['contrast'] > 0.5 else 'Low',
        'homogeneity': 'Good' if features['homogeneity'] > 0.6 else 'Poor',
        'energy': 'Uniform' if features['energy'] > 0.4 else 'Non-uniform',
        'correlation': 'Strong' if features['correlation'] > 0.7 else 'Weak',
        'dissimilarity': 'High' if features['dissimilarity'] > 0.5 else 'Low',
        'entropy': 'Complex' if features['entropy'] > 3.0 else 'Simple'
    }

    # Calculate weighted quality score (0-100)
    quality_score = sum(weights[feature] * features[feature] * 100
                        for feature in weights.keys())

    return quality_score, analysis
