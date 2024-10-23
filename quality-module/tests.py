import numpy as np
from matplotlib import pyplot as plt
from scipy import ndimage

import ridge_frequency

angle = np.radians(62)  # Rotate by 30 degrees
original_rect = np.zeros((200, 300))

original_rect[50:150, 100:200] = 1

rotated_cropped_rect = ridge_frequency.rotate_and_crop(original_rect, angle)

fig, axs = plt.subplots(1, 3, figsize=(15, 5))

# Original Rectangle
axs[0].imshow(original_rect, cmap='gray', extent=[0, 300, 200, 0])
axs[0].set_title('Original Rectangle')
axs[0].axis('off')

# Rotated Rectangle
rotated_rect = ndimage.rotate(original_rect, angle=np.degrees(angle), reshape=False)
axs[1].imshow(rotated_rect, cmap='gray', extent=[-150, 150, -150, 150])
axs[1].set_title('Rotated Rectangle')
axs[1].axis('off')

# Cropped Rotated Rectangle
axs[2].imshow(rotated_cropped_rect, cmap='gray',
              extent=[0, rotated_cropped_rect.shape[1], rotated_cropped_rect.shape[0], 0])
axs[2].set_title('Cropped Rotated Rectangle')
axs[2].axis('off')

plt.show()
