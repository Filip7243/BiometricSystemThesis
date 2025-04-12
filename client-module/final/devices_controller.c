#include <stdio.h>
#include <pigpio.h>
#include <unistd.h>
#include "devices_controller.h"

void init_lock_and_buzzer()
{
    // Ustawienie GPIO na output (wyjscie)
    gpioSetMode(LOCK_PIN, PI_OUTPUT);
    gpioSetMode(BUZZER_PIN, PI_OUTPUT);
}

void open_lock(int state)
{
    // Kontroluj buzzer i zamek na podstawie stanu
    if (state == 0) // Otwarcie zamka
    {
        gpioWrite(LOCK_PIN, 1);   // Ustawienie pinu na stan wysoki (zamek otwarty)
        gpioWrite(BUZZER_PIN, 1); // Ustawienie pinu na stan niski (buzzer generuje dzwiek)
    }
    else if (state == 1) // Zamkniecie zamka
    {
        gpioWrite(LOCK_PIN, 0);   // Ustawienie pinu na stan niski (zamek zamkniety)
        gpioWrite(BUZZER_PIN, 0); // Ustawienie pinu na stan niski (buzzer nie generuje dzwieku)
    }
    else
    {
        printf("Invalid state. Use 0 to open and 1 to close the lock.\n");
    }
}

/**
 * Zmienia stan pinu ENABLE na wyświetlaczu LCD
 * w celu synchronizacji przesyłania danych. Używane do przesyłania danych w trybie 4 bitowym.
 *
 * Funkcja ta jest wywoływana po każdym
 * przesyłaniu 4-bitowych danych do wyświetlacza (po ustawieniu wyższych i niższych
 * bitów w pinach danych) w celu synchronizacji danych.
 */
void lcd_toggle_enable()
{
    gpioWrite(LCD_E, 1);
    usleep(E_PULSE);
    gpioWrite(LCD_E, 0);
    usleep(E_DELAY);
}

/**
 * Wysyła pojedynczy bajt (8 bitow) danych do wyświetlacza LCD w trybie 4 bitowym,
 * najpierw bity     (MSB), później niskie (LSB).
 *
 * @param bits Bajt danych (8 bitów), który ma zostać wysłany do wyświetlacza.
 * @param mode Tryb (0 - komenda, 1 - dane), który decyduje, czy wysyłane są komendy, czy dane.
 */
void lcd_byte(int bits, int mode)
{
    // Ustawienie pinu RS (tryb komendy/danych)s
    gpioWrite(LCD_RS, mode);

    // Wysyłanie wyższych bitów (4 bity MSB)
    gpioWrite(LCD_D4, (bits & 0x10) >> 4);
    gpioWrite(LCD_D5, (bits & 0x20) >> 5);
    gpioWrite(LCD_D6, (bits & 0x40) >> 6);
    gpioWrite(LCD_D7, (bits & 0x80) >> 7);

    // Zmiana stanu pinu ENABLE (przełączanie)
    lcd_toggle_enable();

    // Wysyłanie niższych bitów (4 bity LSM)
    gpioWrite(LCD_D4, bits & 0x01);
    gpioWrite(LCD_D5, (bits & 0x02) >> 1);
    gpioWrite(LCD_D6, (bits & 0x04) >> 2);
    gpioWrite(LCD_D7, (bits & 0x08) >> 3);

    // Zmiana stanu pinu ENABLE (przełączanie)
    lcd_toggle_enable();
}

void lcd_init()
{
    // Ustawienie GPIO na output (wyjscie)
    gpioSetMode(LCD_RS, PI_OUTPUT);
    gpioSetMode(LCD_E, PI_OUTPUT);
    gpioSetMode(LCD_D4, PI_OUTPUT);
    gpioSetMode(LCD_D5, PI_OUTPUT);
    gpioSetMode(LCD_D6, PI_OUTPUT);
    gpioSetMode(LCD_D7, PI_OUTPUT);

    lcd_byte(INIT_4BIT_2LINES, LCD_CMD);         // Inicjalizacja
    lcd_byte(CONTINUE_INIT, LCD_CMD);            // Inicjalizacja
    lcd_byte(SET_MATRIX_FIVE_BY_EIGHT, LCD_CMD); // 2 line 5x8 matrix
    lcd_byte(DISPLAY_ON_CURSOR_OFF, LCD_CMD);    // Wylaczenie kursora
    lcd_byte(SHIFT_CURSOR_RIGHT, LCD_CMD);       // Przesuniecie kurosra w prawo
    lcd_byte(LCD_CLEARDISPLAY, LCD_CMD);         // Wyczysczenie ekranu

    usleep(E_DELAY);
}

void lcd_string(const char *message, int line)
{
    lcd_byte(line, LCD_CMD);

    for (int i = 0; i < LCD_WIDTH; i++)
    {
        if (message[i] != '\0')
        {
            lcd_byte(message[i], LCD_CHR);
        }
        else
        {
            lcd_byte(' ', LCD_CHR); // Podmiania znaku null na spacje
        }
    }
}

void print_error_message(unsigned long nErrCode)
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

int write_bmp_file(unsigned char *pImage, int width, int height, char *ret_filename)
{
    BITMAPINFO *pDIBHeader;
    BITMAPFILEHEADER bmfHeader;
    int iCyc;

    // Alokowanie pamieci na naglowek pliku BMP
    if ((pDIBHeader = (BITMAPINFO *)malloc(sizeof(BITMAPINFO) + sizeof(RGBQUAD) * 255)) == NULL)
    {
        printf("Alloc memory failed! - Unable to write to file!!\n");
        return -1;
    }

    // Wypełnienie nagłówka pliku BMP
    memset((void *)pDIBHeader, 0, sizeof(BITMAPINFO) + sizeof(RGBQUAD) * 255);
    pDIBHeader->bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
    pDIBHeader->bmiHeader.biWidth = width;
    pDIBHeader->bmiHeader.biHeight = height;
    pDIBHeader->bmiHeader.biPlanes = 1;
    pDIBHeader->bmiHeader.biBitCount = 8;    // 8bits gray scale bmp
    pDIBHeader->bmiHeader.biCompression = 0; // BI_RGB = 0;

    // ustawienie rozdzielczosci obrazy na 500 DPI (standard w systemach biometrycznych)
    long int resolution = (long int)(500 / 0.0254);
    pDIBHeader->bmiHeader.biXPelsPerMeter = resolution;
    pDIBHeader->bmiHeader.biYPelsPerMeter = resolution;

    // Ustawienie kolorów w obrazie (odcienie szarosci)
    for (iCyc = 0; iCyc < 256; iCyc++)
    {
        pDIBHeader->bmiColors[iCyc].rgbBlue = pDIBHeader->bmiColors[iCyc].rgbGreen = pDIBHeader->bmiColors[iCyc].rgbRed = (unsigned char)iCyc;
    }

    // Ustawienei typu obrazu
    bmfHeader.bfType = 0x4D42;                                                           // "BM" heksadecymalnie
    bmfHeader.bfSize = 14 + sizeof(BITMAPINFO) + sizeof(RGBQUAD) * 255 + width * height; // sizeof(BITMAPFILEHEADER) = 14
    bmfHeader.bfOffBits = 14 + pDIBHeader->bmiHeader.biSize + sizeof(RGBQUAD) * 256;

    // Tworzenie nazwy pliku
    time_t now = time(NULL);
    struct tm *timeinfo = localtime(&now);
    strftime(ret_filename, 50, "fingers/finger_%Y%m%d_%H%M%S.bmp", timeinfo);

    FILE *fp = fopen(ret_filename, "wb");
    if (!fp)
    {
        printf("Failed to write to file\n");
        free(pDIBHeader);
        return -1;
    }

    // Zapsiywanie danych obrazu do pliku
    fwrite((void *)&bmfHeader.bfType, sizeof(unsigned short int), 1, fp);
    fwrite((void *)&bmfHeader.bfSize, sizeof(unsigned long int), 1, fp);
    fwrite((void *)&bmfHeader.bfReserved1, sizeof(unsigned short int), 1, fp); // bfReserved1 = 0
    fwrite((void *)&bmfHeader.bfReserved2, sizeof(unsigned short int), 1, fp); // bfReserved2 = 0
    fwrite((void *)&bmfHeader.bfOffBits, sizeof(unsigned long int), 1, fp);
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
        fwrite((void *)&pDIBHeader->bmiColors[iCyc].rgbReserved, sizeof(unsigned char), 1, fp); // rgbReserved = 0
    }

    // Kopiowanie danych obrazu
    unsigned char *cptrData;
    unsigned char *cptrDIBData;
    unsigned char *pDIBData;

    pDIBData = (unsigned char *)malloc(height * width);
    memset((void *)pDIBData, 0, height * width);

    cptrData = pImage + (height - 1) * width;
    cptrDIBData = pDIBData;
    for (iCyc = 0; iCyc < height; iCyc++)
    {
        for (int j = 0; j < width; j++)
        {
            // Odwrocenie wartosci kolorow obrazu (z czarnego tla na biale, z bialego odcisku na czarny)
            cptrDIBData[j] = 255 - cptrData[j];
        }
        cptrData = cptrData - width;
        cptrDIBData = cptrDIBData + width;
    }
    fwrite((void *)pDIBData, 1, width * height, fp);
    fclose(fp);

    printf("Fingerprint image is written to file: %s.\n", ret_filename);

    free(pDIBData);
    free(pDIBHeader);

    return 0;
}