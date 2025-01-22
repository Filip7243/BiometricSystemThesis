#ifndef FILE_UPLOAD_H
#define FILE_UPLOAD_H

#include <finger_types.h>

// Stałe używane przy wysłaniu pliku na serwer
#define UPLOAD_URL "http://192.168.43.29:8080/api/v1/enrollments" // Endpoint na serwerze
// #define UPLOAD_URL "http://10.100.123.26:8080/api/v1/enrollments"
#define IMAGE_TYPE "image/bmp" // Typ przesyłąnego pliku

/**
 * Struktura przechowująca odpowiedź w pamięci.
 * @param response - wskaźnik do przechowywanego danych odpowiedzi
 * @param size - rozmiar przechowywanych danych
 */
struct Memory
{
    char *response;
    size_t size;
};

/**
 * Funkcja wysyłająca request z danymi biometrycznymi na serwer.
 * @param type - typ palca, którego dotyczy przesyłany plik (np. kciuk, palec wskazujący)
 * @param mac_addr - adres MAC urządzenia w formacie szesnastkowym (np. 00:1A:2B:3C:4D:5E)
 * @param filename - nazwa pliku, który ma zostać wysłany
 * @return Zwraca kod statusu operacji (0 - sukces, -1 - błąd)
 */
int send_file(FingerType type, char mac_addr[18], char filename[50]);

/**
 * Funkcja typu callback do CURL do zapisu danych odpowiedzi,
 * uruchamiana po otrzymaniu przez klienta odpowiedzi od serwera
 *
 * @param data - dane, które zostały pobrane przez CURL
 * @param size - rozmiar pojedynczego elementu danych
 * @param nmemb - liczba elementów w danych
 * @param userp - wskaźnik do miejsca zapis danych z odpowiedzi (Memory)
 * @return Zwraca rozmiar przetworzonych danych
 */
size_t write_callback(void *data, size_t size, size_t nmemb, void *userp);

#endif