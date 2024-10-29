import os.path
import shutil
import subprocess
from pathlib import Path

from PIL import Image

folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB')
out_folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB\pngs')
classify_folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB\pngs\classify')
bmp_folder = Path(r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\SOCOFing\Real')
fvc_folder = Path(
    r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\dataset_FVC2000_DB4_B\dataset\real_data')


fvc2002_folder = Path(
    r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002\DB4_B')

fvc2002_out = Path(
    r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002\DB4_B_PNGS')
fvc2002_classify = Path(
    r'C:\Users\Filip\Desktop\STUDIA\PracaInzynierska\fingerprints\fvc2002\DB4_B_PNGS\classify')

tif_files = list(folder.glob('*.tif'))
out_files = list(out_folder.glob('*.png'))
bmp_files = list(bmp_folder.glob('*.BMP'))
fvc_files = list(fvc_folder.glob('*.bmp'))
fvc2002_files = list(fvc2002_folder.glob('*.tif'))
fvc2002_files_out = list(fvc2002_out.glob('*.png'))

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
out_file = 'nfiq_results_fvc2002_db4.txt'
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


def classify_fingerprint_quality(score):
    """Classifies the fingerprint quality based on the score."""
    if score >= 75:
        return "excellent"
    elif 50 <= score < 75:
        return "very_good"
    elif 25 <= score < 50:
        return "good"
    elif 1 <= score < 25:
        return "fair"
    elif score == 0:
        return "poor"
    else:
        return "invalid_score"


os.makedirs(fvc2002_classify, exist_ok=True)
quality_folders = ['excellent', 'very_good', 'good', 'fair', 'poor']
for folder in quality_folders:
    os.makedirs(os.path.join(fvc2002_classify, folder), exist_ok=True)

with open(out_file, 'r') as file:
    for line in file:
        if line.strip():
            path, score_str = line.split(": ")
            score = int(score_str.strip())

            print(f'Path: {path.strip()}')

            path = path.split(" ")
            print(f'Splitted path: {path}')

            print(f'Score: {score}')

            quality = classify_fingerprint_quality(score)
            if quality != "invalid_score":  # Check if the score is valid
                target_folder = os.path.join(fvc2002_classify, quality)
                print(f'Target folder: {target_folder}')
                try:
                    shutil.copy(path[2].strip(), target_folder)
                    print(f"Copied {path[2].strip()} to {target_folder}")
                except Exception as e:
                    print(f"Error copying {path[2].strip()}: {e}")
