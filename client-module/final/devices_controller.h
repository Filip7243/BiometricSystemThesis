#ifndef DEVICES_CONTROLLER_H
#define DEVICES_CONTROLLER_H

#include <stdbool.h>
#include <ftrScanAPI_test.h>
#include <string.h>

// Definicja GPIO dla zamka i buzzera
#define LOCK_PIN 16
#define BUZZER_PIN 12

// Definicje stałych dla LCD
#define LCD_RS 3  // D8
#define LCD_E 2   // D9
#define LCD_D4 26 // D4
#define LCD_D5 19 // D5
#define LCD_D6 13 // D6
#define LCD_D7 6  // D7

#define LCD_WIDTH 16    // Maximum characters per line
#define LCD_CHR 1       // Mode - Sending data
#define LCD_CMD 0       // Mode - Sending command
#define LCD_LINE_1 0x80 // LCD RAM address for the 1st line
#define LCD_LINE_2 0xC0 // LCD RAM address for the 2nd line

#define E_PULSE 500 // Opoznienie na pinie E w mikrosekundach
#define E_DELAY 500 // Opoznienie pomiedzy wyslaniem rozkazu do LCD

// Komendy LCD (zrodlo: https://www.electronicsforu.com/technology-trends/learn-electronics/16x2-lcd-pinout-diagram#rs-register-select)
#define LCD_CLEARDISPLAY 0x01
#define SHIFT_CURSOR_RIGHT 0x06
#define DISPLAY_ON_CURSOR_OFF 0x0C
#define SET_MATRIX_FIVE_BY_EIGHT 0x28
// Inicjalizacja, zawsze 0x33 musi byc przed 0x32
#define INIT_4BIT_2LINES 0x33
#define CONTINUE_INIT 0x32

// Zarzadzanie/komunikacja ze skanerem

/**
 * Struktura opisująca nagłówek informacji pliku BMP.
 * Przechowuje podstawowe dane o obrazie, takie jak rozmiary, kompresja itp.
 */
typedef struct tagBITMAPINFOHEADER
{
    unsigned long int biSize;         // Rozmiar struktury w bajtach
    long int biWidth;                 // Szerokość obrazu w pikselach
    long int biHeight;                // Wysokość obrazu w pikselach
    unsigned short int biPlanes;      // Liczba warstw kolorów (zawsze 1)
    unsigned short int biBitCount;    // Liczba bitów na piksel (np. 8 dla 8-bit grayscale)
    unsigned long int biCompression;  // Typ kompresji
    unsigned long int biSizeImage;    // Rozmiar obrazu w bajtach
    long int biXPelsPerMeter;         // Rozdzielczość pozioma w pikselach na metr
    long int biYPelsPerMeter;         // Rozdzielczość pozioma w pikselach na metr
    unsigned long int biClrUsed;      // Liczba kolorów użytych w palecie (nie używane w projekcie)
    unsigned long int biClrImportant; // Liczba ważnych kolorów (nie używane w projekcie)
} BITMAPINFOHEADER, *PBITMAPINFOHEADER;

/**
 * Struktura opisująca pojedynczy kolor w palecie BMP.
 */
typedef struct tagRGBQUAD
{
    unsigned char rgbBlue;
    unsigned char rgbGreen;
    unsigned char rgbRed;
    unsigned char rgbReserved; // Zarezerwowane (zwykle 0)
} RGBQUAD;

/**
 * Struktura zawierająca informacje o obrazie BMP,
 * łącząca dwie struktury BITMAPINFOHEADER i RGBQUAD.
 */
typedef struct tagBITMAPINFO
{
    BITMAPINFOHEADER bmiHeader;
    RGBQUAD bmiColors[1];
} BITMAPINFO, *PBITMAPINFO;

/**
 * Struktura opisująca nagłówek pliku BMP.
 * Przechowuje ogólne informacje o pliku, takie jak rozmiar i ofset danych obrazu.
 */
typedef struct tagBITMAPFILEHEADER
{
    unsigned short int bfType;      // Typ pliku (zawsze 'BM' dla BMP)
    unsigned long int bfSize;       // Całkowity rozmiar pliku w bajtach
    unsigned short int bfReserved1; // Zarezerwowane (zwykle 0)
    unsigned short int bfReserved2; // Zarezerwowane (zwykle 0)
    unsigned long int bfOffBits;    // Ofset do danych obrazu
} BITMAPFILEHEADER, *PBITMAPFILEHEADER;

/**
 * Inicjalizuje GPIO dla zamka i buzzera.
 */
void init_lock_and_buzzer();

/**
 * Steruje otwieraniem lub zamykaniem zamka.
 * @param state - Stan zamka (1 - zamkniety, 0 - otwarty).
 */
void open_lock(int state);
/**
 * Inicjalizuje wyświetlacz LCD.
 */
void lcd_init();

/**
 * Wyświetla ciąg znaków na określonej linii wyświetlacza LCD.
 * @param message - Tekst do wyświetlenia.
 * @param line - Linia, na której tekst ma zostać wyświetlony (LCD_LINE_1 lub LCD_LINE_2).
 */
void lcd_string(const char *message, int line);

/**
 * Zapisuje obraz w formacie BMP do pliku.
 * @param pImage - Wskaźnik na dane obrazu.
 * @param width - Szerokość obrazu.
 * @param height - Wysokość obrazu.
 * @param ret_filename - Nazwa pliku wyjściowego.
 * @return Kod statusu (0 - sukces, -1 - błąd).
 */
int write_bmp_file(unsigned char *pImage, int width, int height, char *ret_filename);

/**
 * Wyświetla komunikat o błędzie podczas komunikacji ze skanerem na podstawie kodu błędu.
 * @param nErrCode - Kod błędu.
 */
void print_error_message(unsigned long nErrCode);

#endif // DEVICES_CONTROLLER_H