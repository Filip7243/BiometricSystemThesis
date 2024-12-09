#ifndef FILE_UPLOAD_H
#define FILE_UPLOAD_H

#include <finger_types.h>

#define UPLOAD_URL "http://10.100.123.34:8080/api/v1/enrollments"
#define IMAGE_TYPE "image/bmp"
#define IMAGE_NAME "frame_Ex.bmp"

// Function prototype for sending file via CURL
int send_file(FingerType type);

#endif