import csv
import os.path
import shutil
import subprocess
from pathlib import Path

input_folders = ['DB1_B_PNGS', 'DB2_B_PNGS', 'DB3_B_PNGS', 'DB4_B_PNGS', 'cross_match']
output_folders = ['Excellent', 'Good', 'Fair', 'Poor', 'VeryPoor']

data_storage_path = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\analized_data')

base_data_path = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002_and_cross')

nfiq_path = os.path.join(r'C:\Program Files\NFIQ 2\bin', 'nfiq2.exe')

csv_name = 'classified_data.csv'

for folder in output_folders:
    os.makedirs(os.path.join(data_storage_path, folder), exist_ok=True)


def run_nfiq(fingerprint_path):
    try:
        result = subprocess.run([nfiq_path, "-i", fingerprint_path],
                                stdout=subprocess.PIPE,
                                stderr=subprocess.PIPE,
                                text=True)

        if result.returncode != 0:
            print(f"Error running NFIQ: {result.stderr}")

        result = result.stdout.strip()

        print(f'Result: {result}')

        return result

    except Exception as e:
        print(f"An error occurred: {e}")


# Classify images based on NFIQ 2.0
def classify_image(score):
    if score >= 80:
        return 'Excellent'
    elif score >= 60:
        return 'Good'
    elif score >= 40:
        return 'Fair'
    elif score >= 20:
        return 'Poor'
    else:
        return 'VeryPoor'


def safe_int_cast(value):
    """
    Safely cast a value to integer.

    Parameters:
    value: Any value to be converted to integer

    Returns:
    tuple: (success: bool, result: int|None, error_message: str|None)
    """
    try:
        result = int(value)
        return True, result, None
    except ValueError:
        return False, None, f"'{value}' cannot be converted to integer. Please provide a valid number."
    except TypeError:
        return False, None, f"Cannot convert {type(value).__name__} type to integer."
    except Exception as e:
        return False, None, f"Unexpected error: {str(e)}"


for input_folder in input_folders:
    input_files_path = Path(os.path.join(base_data_path, input_folder))
    input_files = list(input_files_path.glob('*.png'))

    data = []
    for input_file in input_files:
        score = run_nfiq(input_file)
        is_number, score, _ = safe_int_cast(score)
        if not is_number:
            score = -1

        quality_class = classify_image(score)

        target_folder = os.path.join(data_storage_path, quality_class)
        print(f'Target folder: {target_folder}')
        try:
            shutil.copy(input_file, target_folder)
            print(f"Copied {input_file} to {target_folder}")
        except Exception as e:
            print(f"Error copying {input_file}: {e}")

        entry = {
            'image_path': input_file,
            'file_name': os.path.basename(input_file),
            'nfiq2_score': score,
            'quality_class': quality_class
        }

        data.append(entry)

    headers = ['image_path', 'file_name', 'nfiq2_score', 'quality_class']

    csv_full_path = os.path.join(data_storage_path, csv_name)
    file_exists = os.path.isfile(csv_full_path)
    with open(csv_full_path, mode='a', newline='', encoding='utf-8') as csv_file:
        print('Saving to csv!')

        writer = csv.DictWriter(csv_file, fieldnames=headers)

        if not file_exists:
            writer.writeheader()

        writer.writerows(data)

        print('Data saved')

# folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB')
# out_folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB\pngs')
# classify_folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB\pngs\classify')
# bmp_folder = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\SOCOFing\Real')
# fvc_folder = Path(
#     r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\dataset_FVC2000_DB4_B\dataset\real_data')
#
# fvc2002_folder = Path(
#     r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002\DB4_B')
#
# fvc2002_out = Path(
#     r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002\DB4_B_PNGS')
# fvc2002_classify = Path(
#     r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002\DB4_B_PNGS\classify')
#
# tif_files = list(folder.glob('*.tif'))
# out_files = list(out_folder.glob('*.png'))
# bmp_files = list(bmp_folder.glob('*.BMP'))
# fvc_files = list(fvc_folder.glob('*.bmp'))
# fvc2002_files = list(fvc2002_folder.glob('*.tif'))
# fvc2002_files_out = list(fvc2002_out.glob('*.png'))

# i = 0
# for img_path in fvc2002_files:
#     print(img_path)
#     with Image.open(img_path) as img:
#         dpi = img.info['dpi']
#         grayscale_img = img.convert("L")
#         print(f'Current dpi: {dpi}')
#
#         grayscale_img.save(f'{fvc2002_out}\\{i}.png', format="PNG", dpi=(500, 500))
#         i += 1
#         print('Saved!')

# nfiq_path = os.path.join(r'C:\Program Files\NFIQ 2\bin', 'nfiq2.exe')
# out_file = 'nfiq_results_fvc2002_db4.txt'


# if not os.path.isfile(nfiq_path):
#     print('WTF?!')
#
# for out_path in fvc2002_files_out:
#     try:
#         result = subprocess.run([nfiq_path, "-i", out_path],
#                                 stdout=subprocess.PIPE,
#                                 stderr=subprocess.PIPE,
#                                 text=True)
#
#         if result.returncode != 0:
#             print(f"Error running NFIQ: {result.stderr}")
#
#         msg = f"Score for {out_path}: {result.stdout.strip()}"
#         print(msg)
#         with open(out_file, "a") as file:
#             file.write(f"Score for {out_path}: {result.stdout.strip()}\n")
#
#     except Exception as e:
#         print(f"An error occurred: {e}")


# def classify_fingerprint_quality(score):
#     """Classifies the fingerprint quality based on the score."""
#     if score >= 75:
#         return "excellent"
#     elif 50 <= score < 75:
#         return "very_good"
#     elif 25 <= score < 50:
#         return "good"
#     elif 1 <= score < 25:
#         return "fair"
#     elif score == 0:
#         return "poor"
#     else:
#         return "invalid_score"
#
#
# os.makedirs(fvc2002_classify, exist_ok=True)
# quality_folders = ['excellent', 'very_good', 'good', 'fair', 'poor']
# for folder in quality_folders:
#     os.makedirs(os.path.join(fvc2002_classify, folder), exist_ok=True)
#
# with open(out_file, 'r') as file:
#     for line in file:
#         if line.strip():
#             path, score_str = line.split(": ")
#             score = int(score_str.strip())
#
#             print(f'Path: {path.strip()}')
#
#             path = path.split(" ")
#             print(f'Splitted path: {path}')
#
#             print(f'Score: {score}')
#
#             quality = classify_fingerprint_quality(score)
#             if quality != "invalid_score":  # Check if the score is valid
#                 target_folder = os.path.join(fvc2002_classify, quality)
#                 print(f'Target folder: {target_folder}')
#                 try:
#                     shutil.copy(path[2].strip(), target_folder)
#                     print(f"Copied {path[2].strip()} to {target_folder}")
#                 except Exception as e:
#                     print(f"Error copying {path[2].strip()}: {e}")
