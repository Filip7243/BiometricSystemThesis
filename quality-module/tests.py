import numpy as np
from matplotlib import pyplot as plt

import ridge_frequency

# Image Preprocessing Utilities
# Lei Mao
# University of Chicago
# 3/1/2018


# def inside_rect(rect, num_cols, num_rows):
#     # Determine if the four corners of the rectangle are inside the rectangle with width and height
#     # rect tuple
#     # center (x,y), (width, height), angle of rotation (to the row)
#     # center  The rectangle mass center.
#     # center tuple (x, y): x is regarding to the width (number of columns) of the image, y is regarding to the height (number of rows) of the image.
#     # size    Width and height of the rectangle.
#     # angle   The rotation angle in a clockwise direction. When the angle is 0, 90, 180, 270 etc., the rectangle becomes an up-right rectangle.
#     # Return:
#     # True: if the rotated sub rectangle is side the up-right rectange
#     # False: else
#
#     rect_center = rect[0]
#     rect_center_x = rect_center[0]
#     rect_center_y = rect_center[1]
#
#     rect_width, rect_height = rect[1]
#
#     rect_angle = rect[2]
#
#     if (rect_center_x < 0) or (rect_center_x > num_cols):
#         return False
#     if (rect_center_y < 0) or (rect_center_y > num_rows):
#         return False
#
#     # https://docs.opencv.org/3.0-beta/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html
#     box = cv2.boxPoints(rect)
#
#     x_max = int(np.max(box[:, 0]))
#     x_min = int(np.min(box[:, 0]))
#     y_max = int(np.max(box[:, 1]))
#     y_min = int(np.min(box[:, 1]))
#
#     if (x_max <= num_cols) and (x_min >= 0) and (y_max <= num_rows) and (y_min >= 0):
#         return True
#     else:
#         return False


# def rect_bbx(rect):
#     # Rectangle bounding box for rotated rectangle
#     # Example:
#     # rotated rectangle: height 4, width 4, center (10, 10), angle 45 degree
#     # bounding box for this rotated rectangle, height 4*sqrt(2), width 4*sqrt(2), center (10, 10), angle 0 degree
#
#     box = cv2.boxPoints(rect)
#
#     x_max = int(np.max(box[:, 0]))
#     x_min = int(np.min(box[:, 0]))
#     y_max = int(np.max(box[:, 1]))
#     y_min = int(np.min(box[:, 1]))
#
#     center = (int((x_min + x_max) // 2), int((y_min + y_max) // 2))
#     width = int(x_max - x_min)
#     height = int(y_max - y_min)
#     angle = 0
#
#     return (center, (width, height), angle)


# def image_rotate_without_crop(mat, angle):
#     # https://stackoverflow.com/questions/22041699/rotate-an-image-without-cropping-in-opencv-in-c
#     # angle in degrees
#
#     height, width = mat.shape[:2]
#     image_center = (width / 2, height / 2)
#
#     rotation_mat = cv2.getRotationMatrix2D(image_center, angle, 1)
#
#     abs_cos = abs(rotation_mat[0, 0])
#     abs_sin = abs(rotation_mat[0, 1])
#
#     bound_w = int(height * abs_sin + width * abs_cos)
#     bound_h = int(height * abs_cos + width * abs_sin)
#
#     rotation_mat[0, 2] += bound_w / 2 - image_center[0]
#     rotation_mat[1, 2] += bound_h / 2 - image_center[1]
#
#     rotated_mat = cv2.warpAffine(mat, rotation_mat, (bound_w, bound_h))
#
#     return rotated_mat


# def crop_rectangle(image, rect):
#     # rect has to be upright
#
#     num_rows = image.shape[0]
#     num_cols = image.shape[1]
#
#     if not inside_rect(rect=rect, num_cols=num_cols, num_rows=num_rows):
#         print("Proposed rectangle is not fully in the image.")
#         return None
#
#     rect_center = rect[0]
#     rect_center_x = rect_center[0]
#     rect_center_y = rect_center[1]
#     rect_width = rect[1][0]
#     rect_height = rect[1][1]
#
#     return image[rect_center_y - rect_height // 2:rect_center_y + rect_height - rect_height // 2,
#            rect_center_x - rect_width // 2:rect_center_x + rect_width - rect_width // 2]


# def crop_rotated_rectangle(image, rect):
#     # Crop a rotated rectangle from a image
#
#     num_rows = image.shape[0]
#     num_cols = image.shape[1]
#
#     if not inside_rect(rect=rect, num_cols=num_cols, num_rows=num_rows):
#         print("Proposed rectangle is not fully in the image.")
#         return None
#
#     rotated_angle = rect[2]
#
#     rect_bbx_upright = rect_bbx(rect=rect)
#     rect_bbx_upright_image = crop_rectangle(image=image, rect=rect_bbx_upright)
#
#     rotated_rect_bbx_upright_image = image_rotate_without_crop(mat=rect_bbx_upright_image, angle=rotated_angle)
#
#     rect_width = rect[1][0]
#     rect_height = rect[1][1]
#
#     crop_center = (rotated_rect_bbx_upright_image.shape[1] // 2, rotated_rect_bbx_upright_image.shape[0] // 2)
#
#     return rotated_rect_bbx_upright_image[
#            crop_center[1] - rect_height // 2: crop_center[1] + (rect_height - rect_height // 2),
#            crop_center[0] - rect_width // 2: crop_center[0] + (rect_width - rect_width // 2)]


# def crop_rotated_rectangle_test():
#     # Test function for crop_rotated_rectangle(image, rect)
#
#     from matplotlib import gridspec
#
#     # Better to test in Jupyter Notebook
#
#     folder = Path(r'C:\Users\Filip\Desktop\STUDIA\inzynierka\CrossMatch_Sample_DB')
#     tif_files = list(folder.glob('*.png'))
#     img_cntrs = cv2.imread(tif_files[0])
#     img_cntrs = cv2.cvtColor(img_cntrs, cv2.COLOR_BGR2RGB)
#
#     img = utils.read_image(tif_files[0])
#     img = utils.normalize_image(img)
#
#     print('Finding Mask')
#     mask = utils.segment_fingerprint(img)
#
#     orientations, coherence = img_orientation.estimate_orientation(img, _interpolate=True)
#     orientations = np.where(mask == 1.0, orientations, -1.0)
#
#     h = img.shape[0]
#     w = img.shape[1]
#
#     y_blocks, x_blocks = h // 32, w // 32
#
#     for j in range(y_blocks):
#         for i in range(x_blocks):
#             y_slice = j * 32 + 32 // 2  # Horizontal center
#             x_slice = i * 32 + 32 // 2  # Vertical center
#
#             block_orientation = orientations[y_slice, x_slice]
#             y_start = j * 32
#             y_end = (j + 1) * 32
#             x_start = i * 32
#             x_end = (i + 1) * 32
#             block_img = img[y_start: y_end, x_start: x_end]
#             # TODO: score based on frequencies
#             rect = ((y_slice, x_slice), (32, 32), np.degrees(np.pi * 0.5 + block_orientation))
#
#             # while True:
#             #     center = (np.random.randint(low=1, high=w), np.random.randint(low=0, high=h))
#             #     angle = 31.21
#             #     rect = (center, (32, 32), angle)
#             #     if inside_rect(rect=rect, num_cols=w, num_rows=h):
#             #         break
#
#             box = cv2.boxPoints(rect).astype(np.intp)
#
#             cv2.drawContours(img_cntrs, [box], 0, (255, 0, 0), 3)
#             image_cropped = crop_rotated_rectangle(image=img_cntrs, rect=rect)
#
#             # plot it
#             gs = gridspec.GridSpec(1, 2, width_ratios=[3, 1])
#             ax0 = plt.subplot(gs[0])
#             ax0.imshow(img_cntrs)
#             ax1 = plt.subplot(gs[1])
#             if image_cropped is not None and image_cropped.size > 0:
#                 ax1.imshow(image_cropped)
#
#             plt.tight_layout()
#
#             plt.show()
#
#     return

#
# if __name__ == '__main__':
#     crop_rotated_rectangle_test()

original_rect = np.zeros((200, 300))
original_rect[50:150, 100:200] = 1  # Rectangle coordinates

# Angle to rotate (in radians)
angle = np.radians(55)  # Rotate by 30 degrees

# Rotate and crop the image
processed_image = ridge_frequency.rotate_and_crop(original_rect, angle)

# Plotting the original and processed image
plt.figure(figsize=(10, 5))
plt.subplot(1, 2, 1)
plt.title("Original Image")
plt.imshow(original_rect, cmap='gray', vmin=0, vmax=1)
plt.axis('off')

plt.subplot(1, 2, 2)
plt.title("Processed Image")
plt.imshow(processed_image, cmap='gray', vmin=0, vmax=1)
plt.axis('off')

plt.tight_layout()
plt.show()
