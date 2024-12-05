#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <ftrScanAPI_test.h>

#include <json-c/json.h>
#include <curl/curl.h>

typedef struct tagBITMAPINFOHEADER
{
	unsigned long int biSize;
	long int biWidth;
	long int biHeight;
	unsigned short int biPlanes;
	unsigned short int biBitCount;
	unsigned long int biCompression;
	unsigned long int biSizeImage;
	long int biXPelsPerMeter;
	long int biYPelsPerMeter;
	unsigned long int biClrUsed;
	unsigned long int biClrImportant;
} BITMAPINFOHEADER, *PBITMAPINFOHEADER;

typedef struct tagRGBQUAD
{
	unsigned char rgbBlue;
	unsigned char rgbGreen;
	unsigned char rgbRed;
	unsigned char rgbReserved;
} RGBQUAD;

typedef struct tagBITMAPINFO
{
	BITMAPINFOHEADER bmiHeader;
	RGBQUAD bmiColors[1];
} BITMAPINFO, *PBITMAPINFO;

typedef struct tagBITMAPFILEHEADER
{
	unsigned short int bfType;
	unsigned long int bfSize;
	unsigned short int bfReserved1;
	unsigned short int bfReserved2;
	unsigned long int bfOffBits;
} BITMAPFILEHEADER, *PBITMAPFILEHEADER;

int send_fingerprint_to_server(unsigned char *pImage, int img_size, const char *hardware_id)
{
	CURL *curl;
	CURLcode res;
	struct curl_slist *headers = NULL; // Request headers

	// Create JSON object to send to server
	json_object *json_obj = json_object_new_object();
	json_object *token = json_object_new_array(); // Fingerprint bytes

	for (int i = 0; i < img_size; i++)
	{
		json_object_array_add(token, json_object_new_int(pImage[i]));
	}

	json_object_object_add(json_obj, "token", token);
	json_object_object_add(json_obj, "type", json_object_new_string("INDEX"));			  // TODO: add logic to get random finger type
	json_object_object_add(json_obj, "hardware_id", json_object_new_string(hardware_id)); // TODO: add logic to get hardware id

	const char *json_str = json_object_to_json_string(json_obj);

	curl = curl_easy_init();

	if (curl)
	{
		curl_easy_setopt(curl, CURLOPT_URL, "http://10.100.123.34:8080/api/v1/enrollments");

		headers = curl_slist_append(headers, "Content-Type: application/json");

		curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
		curl_easy_setopt(curl, CURLOPT_POSTFIELDS, json_str);

		res = curl_easy_perform(curl);

		if (res != CURLE_OK)
		{
			fprintf(stderr, "curl_easy_perform() failed: %s\n", curl_easy_strerror(res));
			curl_easy_cleanup(curl);
			curl_slist_free_all(headers);
			json_object_put(json_obj);

			return -1;
		}

		curl_easy_cleanup(curl);
		curl_slist_free_all(headers);
	}
	else
	{
		fprintf(stderr, "Failed to initialize curl\n");
		return -1;
	}

	json_object_put(json_obj);
	curl_global_cleanup();

	return 0;
}

int write_bmp_file(unsigned char *pImage, int width, int height)
{
	BITMAPINFO *pDIBHeader;
	BITMAPFILEHEADER bmfHeader;
	int iCyc;

	// allocate memory for a DIB header
	if ((pDIBHeader = (BITMAPINFO *)malloc(sizeof(BITMAPINFO) + sizeof(RGBQUAD) * 255)) == NULL)
	{
		printf("Alloc memory failed! - Unable to write to file!!\n");
		return -1;
	}
	memset((void *)pDIBHeader, 0, sizeof(BITMAPINFO) + sizeof(RGBQUAD) * 255);
	// fill the DIB header
	pDIBHeader->bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
	pDIBHeader->bmiHeader.biWidth = width;
	pDIBHeader->bmiHeader.biHeight = height;
	pDIBHeader->bmiHeader.biPlanes = 1;
	pDIBHeader->bmiHeader.biBitCount = 8;	 // 8bits gray scale bmp
	pDIBHeader->bmiHeader.biCompression = 0; // BI_RGB = 0;
	// initialize logical and DIB grayscale
	for (iCyc = 0; iCyc < 256; iCyc++)
	{
		pDIBHeader->bmiColors[iCyc].rgbBlue = pDIBHeader->bmiColors[iCyc].rgbGreen = pDIBHeader->bmiColors[iCyc].rgbRed = (unsigned char)iCyc;
	}
	// set BITMAPFILEHEADER structure
	//((char *)(&bmfHeader.bfType))[0] = 'B';
	//((char *)(&bmfHeader.bfType))[1] = 'M';
	bmfHeader.bfType = 0x42 + 0x4D * 0x100;
	bmfHeader.bfSize = 14 + sizeof(BITMAPINFO) + sizeof(RGBQUAD) * 255 + width * height; // sizeof( BITMAPFILEHEADER ) = 14
	bmfHeader.bfOffBits = 14 + pDIBHeader->bmiHeader.biSize + sizeof(RGBQUAD) * 256;
	// write to file
	FILE *fp;
	fp = fopen("frame_Ex.bmp", "wb");
	if (fp == NULL)
	{
		printf("Failed to write to file\n");
		free(pDIBHeader);
		return -1;
	}
	// fwrite( (void *)&bmfHeader, 1, sizeof(BITMAPFILEHEADER), fp );
	fwrite((void *)&bmfHeader.bfType, sizeof(unsigned short int), 1, fp);
	fwrite((void *)&bmfHeader.bfSize, sizeof(unsigned long int), 1, fp);
	fwrite((void *)&bmfHeader.bfReserved1, sizeof(unsigned short int), 1, fp);
	fwrite((void *)&bmfHeader.bfReserved2, sizeof(unsigned short int), 1, fp);
	fwrite((void *)&bmfHeader.bfOffBits, sizeof(unsigned long int), 1, fp);
	// fwrite( (void *)pDIBHeader, 1, sizeof( BITMAPINFO ) + sizeof( RGBQUAD ) * 255, fp );
	fwrite((void *)&pDIBHeader->bmiHeader.biSize, sizeof(unsigned long int), 1, fp);
	fwrite((void *)&pDIBHeader->bmiHeader.biWidth, sizeof(long int), 1, fp);
	fwrite((void *)&pDIBHeader->bmiHeader.biHeight, sizeof(long int), 1, fp);
	fwrite((void *)&pDIBHeader->bmiHeader.biPlanes, sizeof(unsigned short int), 1, fp);
	fwrite((void *)&pDIBHeader->bmiHeader.biBitCount, sizeof(unsigned short int), 1, fp);
	fwrite((void *)&pDIBHeader->bmiHeader.biCompression, sizeof(unsigned long int), 1, fp);
	fwrite((void *)&pDIBHeader->bmiHeader.biSizeImage, sizeof(unsigned long int), 1, fp);
	fwrite((void *)&pDIBHeader->bmiHeader.biXPelsPerMeter, sizeof(long int), 1, fp);
	fwrite((void *)&pDIBHeader->bmiHeader.biYPelsPerMeter, sizeof(long int), 1, fp);
	fwrite((void *)&pDIBHeader->bmiHeader.biClrUsed, sizeof(unsigned long int), 1, fp);
	fwrite((void *)&pDIBHeader->bmiHeader.biClrImportant, sizeof(unsigned long int), 1, fp);
	for (iCyc = 0; iCyc < 256; iCyc++)
	{
		fwrite((void *)&pDIBHeader->bmiColors[iCyc].rgbBlue, sizeof(unsigned char), 1, fp);
		fwrite((void *)&pDIBHeader->bmiColors[iCyc].rgbGreen, sizeof(unsigned char), 1, fp);
		fwrite((void *)&pDIBHeader->bmiColors[iCyc].rgbRed, sizeof(unsigned char), 1, fp);
		fwrite((void *)&pDIBHeader->bmiColors[iCyc].rgbReserved, sizeof(unsigned char), 1, fp);
	}
	//
	// copy fingerprint image
	unsigned char *cptrData;
	unsigned char *cptrDIBData;
	unsigned char *pDIBData;

	pDIBData = (unsigned char *)malloc(height * width);
	memset((void *)pDIBData, 0, height * width);

	cptrData = pImage + (height - 1) * width;
	cptrDIBData = pDIBData;
	for (iCyc = 0; iCyc < height; iCyc++)
	{
		memcpy(cptrDIBData, cptrData, width);
		cptrData = cptrData - width;
		cptrDIBData = cptrDIBData + width;
	}
	fwrite((void *)pDIBData, 1, width * height, fp);
	fclose(fp);
	printf("Fingerprint image is written to file: frame_Ex.bmp.\n");
	free(pDIBData);
	free(pDIBHeader);
	return 0;
}

void PrintErrorMessage(unsigned long nErrCode)
{
	printf("Failed to obtain image. ");

	char stError[64];

	switch (nErrCode)
	{
	case 0:
		strcpy(stError, "OK");
		break;
	case FTR_ERROR_EMPTY_FRAME: // ERROR_EMPTY
		strcpy(stError, "- Empty frame -");
		break;
	case FTR_ERROR_MOVABLE_FINGER:
		strcpy(stError, "- Movable finger -");
		break;
	case FTR_ERROR_NO_FRAME:
		strcpy(stError, "- Fake finger -");
		break;
	case FTR_ERROR_HARDWARE_INCOMPATIBLE:
		strcpy(stError, "- Incompatible hardware -");
		break;
	case FTR_ERROR_FIRMWARE_INCOMPATIBLE:
		strcpy(stError, "- Incompatible firmware -");
		break;
	case FTR_ERROR_INVALID_AUTHORIZATION_CODE:
		strcpy(stError, "- Invalid authorization code -");
		break;
	default:
		sprintf(stError, "Unknown return code - %lu", nErrCode);
	}
	printf("%s\n", stError);
}

int main(int argc, char *argv[])
{
	void *hDevice;
	FTRSCAN_IMAGE_SIZE ImageSize;
	unsigned char *pBuffer;
	int i;

	curl_global_init(CURL_GLOBAL_ALL);
	const char *hardware_id = "scanner_room_001";

	hDevice = ftrScanOpenDevice();
	if (hDevice == NULL)
	{
		printf("Failed to open device!\n");
		return -1;
	}

	if (!ftrScanGetImageSize(hDevice, &ImageSize))
	{
		printf("Failed to get image size\n");
		ftrScanCloseDevice(hDevice);
		return -1;
	}
	else
	{
		printf("Image size is %d\n", ImageSize.nImageSize);
		pBuffer = (unsigned char *)malloc(ImageSize.nImageSize);
		printf("Please put your finger on the scanner:\n");
		while (1)
		{
			if (ftrScanIsFingerPresent(hDevice, NULL))
				break;
			for (i = 0; i < 100; i++)
				; // sleep
		}
		printf("Capturing fingerprint ......\n");
		while (1)
		{
			if (ftrScanGetFrame(hDevice, pBuffer, NULL))
			{
				printf("Done!\nWriting to file......\n");
				write_bmp_file(pBuffer, ImageSize.nWidth, ImageSize.nHeight);

				if (send_fingerprint_to_server(pBuffer, ImageSize.nImageSize, hardware_id) == 0)
				{
					printf("Fingerprint successfully sent to server!\n");
				}
				else
				{
					printf("Failed to send fingerprint to server\n");
				}

				break;
			}
			else
			{
				PrintErrorMessage(ftrScanGetLastError());
				for (i = 0; i < 100; i++)
					;
			}
		}
		free(pBuffer);
	}

	ftrScanCloseDevice(hDevice);
	return 0;
}
