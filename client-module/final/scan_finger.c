#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <time.h>

#include <finger_types.h>
#include <send.h>
#include <get_device_mac.h>
#include <pigpio.h>
#include <devices_controller.h>

// Funkcja główna programu klienckiego
int main(int argc, char *argv[])
{
    // Initialize pigpio
    if (gpioInitialise() < 0)
    {
        fprintf(stderr, "pigpio initialization failed\n");
        return 1;
    }

    // Inicjalizacja urzadzen peryferyjnych
    init_lock_and_buzzer();
    lcd_init();

    // Pobranie adresu MAC urzadzenia
    char mac_address[18];
    get_mac("wlan0", mac_address);

    // Definicja zmiennych potrzebnych do komunikacji ze skanerem
    void *hDevice;
    FTRSCAN_IMAGE_SIZE ImageSize;
    unsigned char *pBuffer;
    int i;

    // Rozpoczecie komunikacji ze skanerem linii papilarnych
    hDevice = ftrScanOpenDevice();
    if (hDevice == NULL)
    {
        printf("Failed to open device!\n");
        return -1;
    }

    // Pobranie rozmiaru obrazu
    if (!ftrScanGetImageSize(hDevice, &ImageSize))
    {
        printf("Failed to get image size\n");
        ftrScanCloseDevice(hDevice);
        return -1;
    }
    else
    {
        // Zaalokowanie miejsca na bufor na obraz
        pBuffer = (unsigned char *)malloc(ImageSize.nImageSize);

        while (1) // Główna pętla programu
        {
            // Wylosowanie typou palca do skanu
            FingerType randomFinger = get_random_finger();
            const char *fingerType = finger_to_string(randomFinger);

            // Stworzenie bufora na teskt wytswietlany na ekranie LCD
            char full_message[LCD_WIDTH + 1]; // +1 na znak null (terminator)
            full_message[0] = '\0';

            // Zapisanie tekstu w zmiennej full_message
            snprintf(full_message, LCD_WIDTH + 1, "%s%s", "Put your: ", fingerType);

            // Wyświetlenie tekstu na ekranie LCD o palcu do przyłożenia
            lcd_string(full_message, LCD_LINE_1);
            lcd_string("On Scanner!\0", LCD_LINE_2);
            usleep(500000); // 0.5 sec

            // Czekanie na obecność palca na skanerze
            while (1)
            {
                if (ftrScanIsFingerPresent(hDevice, NULL))
                    break;
                for (int i = 0; i < 100; i++)
                    ; // czekanie...
            }

            lcd_string("Capturing\0", LCD_LINE_1);
            lcd_string("fingerprint...", LCD_LINE_2);
            usleep(500000); // 0.5 sec

            // Przechwytywanie obrazu odcisku palca
            while (1)
            {
                // Pobieranie klatki obrazu z odciskiem
                if (ftrScanGetFrame(hDevice, pBuffer, NULL))
                {
                    lcd_string("Scanning done,", LCD_LINE_1);
                    lcd_string("Wait...\0  ", LCD_LINE_2);
                    usleep(500000); // 0.5 sec

                    // Zapisanie pliku BMP z obrazem daktyloskopijnym
                    char filename[50];
                    write_bmp_file(pBuffer, ImageSize.nWidth, ImageSize.nHeight, filename);

                    send_file(randomFinger, mac_address, filename);

                    break; // Wyjście z wewnętrznej pętli, aby powrócić do losowania palca
                }
                else
                {
                    // Niepowodzenie pobrania obrazu
                    print_error_message(ftrScanGetLastError());
                    for (int i = 0; i < 100; i++)
                        ;
                }
            }
        }

        // Wyczyszczenie bufora na zakonczenie programu
        free(pBuffer);
    }

    // Zamknięcie urządzenia
    ftrScanCloseDevice(hDevice);
    gpioTerminate();

    return 0;
}