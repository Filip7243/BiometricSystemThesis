import pandas as pd
from matplotlib import pyplot as plt

# Path to the CSV file
csv_file_path = r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\analized_data\v3\quality_metrics_v3.csv'

# Read the CSV file into a DataFrame
df = pd.read_csv(csv_file_path)

# List of metrics to analyze
metrics = ['mean', 'variance', 'median', 'std_dev', 'min', 'max', 'clarity_mean', 'clarity_std', 'freq_strength_mean', 'freq_strength_std']

# List of quality classes
quality_classes = df['quality_class'].unique()

# Create a dictionary to store the results
results = {}

# Loop through each metric and calculate the statistics for each quality class
for metric in df['metric'].unique():
    metric_data = df[df['metric'] == metric]
    results[metric] = metric_data.groupby('quality_class')[metrics].describe()

# Display the results
for metric, result in results.items():
    for stat in metrics:
        plt.figure(figsize=(10, 6))
        result[stat]['mean'].plot(kind='bar', color='skyblue')
        plt.title(f'{stat.capitalize()} by Quality Class for {metric}')
        plt.xlabel('Quality Class')
        plt.ylabel(stat.capitalize())
        plt.tight_layout()
        plt.show()