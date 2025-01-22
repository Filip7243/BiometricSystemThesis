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
    size_t total_size = size * nmemb;
    struct Memory *mem = (struct Memory *)userp;

    // Reallocate memory for the new data
    char *ptr = realloc(mem->response, mem->size + total_size + 1);
    if (ptr == NULL)
    {
        fprintf(stderr, "Not enough memory to allocate response buffer\n");
        return 0;
    }

    mem->response = ptr;
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
    const char *secret = "YourSecretKey123";
    unsigned char iv[AES_BLOCK_SIZE];
    char encrypted_filename[64] = "encrypted_file.dat";

    // Generuj IV
    generate_iv(iv);

    // Zaszyfruj plik wejściowy
    printf("Encrypting file...\n");
    if (encrypt_aes(filename, encrypted_filename, secret, iv) != 0)
    {
        fprintf(stderr, "Error: File encryption failed\n");
        return -1;
    }

    curl_global_init(CURL_GLOBAL_ALL);
    curl = curl_easy_init();

    if (curl)
    {
        // Create form
        curl_mime *mime;
        curl_mimepart *part;

        mime = curl_mime_init(curl);

        // Add encrypted file part - zmiana typu MIME na application/octet-stream
        part = curl_mime_addpart(mime);
        curl_mime_filedata(part, encrypted_filename);
        curl_mime_name(part, "file");
        curl_mime_type(part, "application/octet-stream");
        printf("Added encrypted file to mime: %s\n", encrypted_filename);

        // Add type part
        part = curl_mime_addpart(mime);
        curl_mime_name(part, "type");
        curl_mime_data(part, finger_to_string(type), CURL_ZERO_TERMINATED);

        // Add hardwareId part
        part = curl_mime_addpart(mime);
        curl_mime_name(part, "hardwareId");
        curl_mime_data(part, mac_addr, CURL_ZERO_TERMINATED);

        // Ustawienia curl
        curl_easy_setopt(curl, CURLOPT_URL, UPLOAD_URL);
        curl_easy_setopt(curl, CURLOPT_MIMEPOST, mime);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void *)&chunk);

        // Wyłącz follow location aby uniknąć przekierowań
        curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 0L);

        // Dodaj nagłówek Content-Type
        struct curl_slist *headers = NULL;
        headers = curl_slist_append(headers, "Content-Type: multipart/form-data");
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);

        printf("Sending request...\n");
        res = curl_easy_perform(curl);

        if (res != CURLE_OK)
        {
            fprintf(stderr, "curl_easy_perform() failed: %s\n",
                    curl_easy_strerror(res));
        }
        else
        {
            long response_code;
            curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &response_code);
            printf("HTTP response code: %ld\n", response_code);

            // Parse and print the response
            if (chunk.response)
            {
                printf("Server response: %s\n", chunk.response);

                struct JsonResponse resp = parse_json_response(chunk.response);
                printf("Parsed response:\n");
                printf("Success: %s\n", resp.success ? "true" : "false");
                printf("Message: %s\n", resp.message);
                printf("Name of User: %s\n", resp.nameOfUser);

                // Open or close the lock based on the response
                if (resp.success)
                {
                    // Stworzenie bufora na połączony tekst
                    char full_message[LCD_WIDTH + 1]; // +1 na znak null (terminator)

                    // Zainicjalizowanie pełnej wiadomości pustym ciągiem
                    full_message[0] = '\0';

                    // Zapisz tekst w zmiennej full_message, nie przekraczając rozmiaru LCD_WIDTH
                    int max_name_length = LCD_WIDTH - strlen("Dear, ");
                    snprintf(full_message, LCD_WIDTH + 1, "%s%.*s", "Dear, ", max_name_length, resp.nameOfUser);

                    lcd_string("Welcome back! ", LCD_LINE_1);
                    lcd_string(full_message, LCD_LINE_2);
                    sleep(1);
                    open_lock(0); // Open the lock
                    sleep(3);     // Wait for 2 seconds
                    open_lock(1); // Close the lock
                }
                else
                {
                    lcd_string("You have no access", LCD_LINE_1);
                    lcd_string("To this room!", LCD_LINE_2);
                    sleep(1);
                }
            }
        }

        // Clean up
        curl_slist_free_all(headers);
        curl_mime_free(mime);
        curl_easy_cleanup(curl);
        free(chunk.response);
    }

    // Usuń tymczasowy plik
    printf("Removing temporary file: %s\n", encrypted_filename);
    remove(encrypted_filename);

    curl_global_cleanup();

    printf("=== Upload completed ===\n\n");
    return 0;
}