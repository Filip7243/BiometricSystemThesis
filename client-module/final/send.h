#ifndef FILE_UPLOAD_H
#define FILE_UPLOAD_H

#include <finger_types.h>

#define UPLOAD_URL "http://192.168.43.29:8080/api/v1/enrollments"
// #define UPLOAD_URL "http://10.100.123.26:8080/api/v1/enrollments"
#define IMAGE_TYPE "image/bmp"
#define IMAGE_NAME "frame_Ex.bmp"

struct Memory
{
    char *response;
    size_t size;
};

// Function prototype for sending file via CURL
int send_file(FingerType type, char mac_addr[18], char filename[50]);
size_t write_callback(void *data, size_t size, size_t nmemb, void *userp);

#endif