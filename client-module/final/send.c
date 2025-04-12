#include <curl/curl.h>
#include <finger_types.h>
#include <send.h>
#include <json_parser.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <devices_controller.h>

#include "data_encryption.h"

size_t write_callback(void *data, size_t size, size_t nmemb, void *userp)
{
    // Całkowity rozmiar pobranych danych
    size_t total_size = size * nmemb;

    // Przypisanie pod userp struktury Memory, ktora przechowuje dane z odpowiedzi
    struct Memory *mem = (struct Memory *)userp;

    // Realokacja pamięci dla nowych danych
    char *ptr = realloc(mem->response, mem->size + total_size + 1);
    if (ptr == NULL)
    {
        fprintf(stderr, "Not enough memory to allocate response buffer\n");
        return 0;
    }

    // Zaktualizowanie wskaźnika na odpowiedź
    mem->response = ptr;

    // Skopiowanie nowych danych do zarezerwowanej pamięci, czyli
    // Dodajemy do istniejacych juz danych kolejne na koncu tych juz istniejacych
    memcpy(&(mem->response[mem->size]), data, total_size);
    mem->size += total_size;
    mem->response[mem->size] = '\0';

    return total_size;
}

int send_file(FingerType type, char mac_addr[18], char filename[50])
{
    CURL *curl;
    CURLcode res;
    struct Memory chunk = {NULL, 0};

    printf("\n=== Starting file upload ===\n");

    // Parametry szyfrowania
    const char *secret = "YourSecretKey123";            // Klucz do szyfrowania (taki sam powinien byc na serwerze)
    unsigned char iv[AES_BLOCK_SIZE];                   // Inicjalizator miejsca w pamieci dla wektora (IV)
    char encrypted_filename[64] = "encrypted_file.dat"; // Nazwa pliku po zaszyfrowaniu

    // Generowanie IV
    generate_iv(iv);

    // Szyfrowanie pliku wejściowego
    if (encrypt_aes(filename, encrypted_filename, secret, iv) != 0)
    {
        fprintf(stderr, "Error: File encryption failed\n");
        return -1;
    }

    // Inicjalizacja CURL
    curl_global_init(CURL_GLOBAL_ALL);
    curl = curl_easy_init();

    if (curl)
    {
        // Tworzenie formularza MIME
        curl_mime *mime;
        curl_mimepart *part;

        mime = curl_mime_init(curl);

        // Dodanie zaszyfrowanego pliku do formularza MIME
        part = curl_mime_addpart(mime);
        curl_mime_filedata(part, encrypted_filename);
        curl_mime_name(part, "file");
        curl_mime_type(part, "application/octet-stream");

        // Dodanie typu palca do formularza
        part = curl_mime_addpart(mime);
        curl_mime_name(part, "type");
        curl_mime_data(part, finger_to_string(type), CURL_ZERO_TERMINATED);

        // Dodanie adresu MAC urządzenia
        part = curl_mime_addpart(mime);
        curl_mime_name(part, "hardwareId");
        curl_mime_data(part, mac_addr, CURL_ZERO_TERMINATED);

        // Ustawienia CURL
        curl_easy_setopt(curl, CURLOPT_URL, UPLOAD_URL);               // Endpoint
        curl_easy_setopt(curl, CURLOPT_MIMEPOST, mime);                // Formularz z danymi
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback); // Funkcja callback do zapisu odpowiedzi
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void *)&chunk);     // Dane z response (struktura Memory)

        // Wyłączenie follow location aby uniknąć przekierowań
        curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 0L);

        // Dodanie nagłówek Content-Type
        struct curl_slist *headers = NULL;
        headers = curl_slist_append(headers, "Content-Type: multipart/form-data");
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);

        res = curl_easy_perform(curl);

        if (res != CURLE_OK)
        {
            fprintf(stderr, "curl_easy_perform() failed: %s\n",
                    curl_easy_strerror(res));
        }
        else
        {
            long response_code;
            curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &response_code); // Odczytanie kodu odpowiedzi HTTP

            // Parsowanie odpowiedzi serwera
            if (chunk.response)
            {
                struct JsonResponse resp = parse_json_response(chunk.response);

                if (resp.success) // Identyfikacja przebiegła pomyślnie - otwarcie zamka
                {
                    // Stworzenie bufora na połączony tekst
                    char full_message[LCD_WIDTH + 1]; // +1 na znak null (terminator)

                    // Zainicjalizowanie wiadomości
                    full_message[0] = '\0';

                    // Zapisanie tekstu w zmiennej full_message
                    int max_name_length = LCD_WIDTH - strlen("Dear, ");
                    // Jesli imie jest za dlugie to zostanie przyciete
                    snprintf(full_message, LCD_WIDTH + 1, "%s%.*s", "Dear, ", max_name_length, resp.nameOfUser);

                    // Wyświetlenie na ekranie LCD
                    lcd_string("Welcome back! ", LCD_LINE_1);
                    lcd_string(full_message, LCD_LINE_2);
                    sleep(1);

                    open_lock(0); // Otwórz zamek
                    sleep(3);     // Czekaj przez 3 sekundy
                    open_lock(1); // Zamknij zamek
                }
                else // Identyfikacja przebiegła niepomyślnie - zamkniecie zamka
                {
                    // Wyświetlenie na ekranie LCD
                    lcd_string("You have no access", LCD_LINE_1);
                    lcd_string("To this room!", LCD_LINE_2);
                    sleep(1);
                }
            }
        }

        // Czyszcczenie CURL
        curl_slist_free_all(headers);
        curl_mime_free(mime);
        curl_easy_cleanup(curl);
        free(chunk.response);
    }

    // Usuwanie tymczasowego pliku zaszyfrowanego
    remove(encrypted_filename);

    // Czyszczenie globalnych zasobów CURL
    curl_global_cleanup();

    printf("=== Upload completed ===\n\n");
    return 0;
}