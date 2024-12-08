from PIL import Image

# Load the image
image_path = "frame_Ex.bmp"  # Replace with your image file path
image = Image.open(image_path)
# image.save("out.bmp",  dpi=(500, 500))
# image = Image.open("out.bmp").convert("L")

# Get basic information
width, height = image.size  # Resolution in pixels (width x height)
dpi = image.info.get('dpi', (None, None))  # DPI (dots per inch), returns a tuple (x, y)

# Print the information
print(f"Resolution: {width} x {height} pixels")
if dpi != (None, None):
    print(f"DPI: {dpi[0]} x {dpi[1]}")
else:
    print("DPI information not available.")

# Calculate PPI (pixels per inch) if DPI is available
if dpi != (None, None):
    ppi_x = dpi[0]
    ppi_y = dpi[1]
    print(f"PPI (Pixels per Inch): {ppi_x} x {ppi_y}")
