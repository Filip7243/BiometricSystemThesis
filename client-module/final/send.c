#include <curl/curl.h>
#include <finger_types.h>
#include <send.h>
#include <json_parser.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <devices_controller.h>

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

    curl_global_init(CURL_GLOBAL_ALL);
    curl = curl_easy_init();

    if (curl)
    {
        // Create form
        curl_mime *mime;
        curl_mimepart *part;

        mime = curl_mime_init(curl);

        // Add file part
        part = curl_mime_addpart(mime);
        curl_mime_filedata(part, filename);
        curl_mime_name(part, "file");
        curl_mime_type(part, IMAGE_TYPE);

        // Add type part
        part = curl_mime_addpart(mime);
        curl_mime_name(part, "type");
        curl_mime_data(part, finger_to_string(type), CURL_ZERO_TERMINATED);

        // Add hardwareId part
        part = curl_mime_addpart(mime);
        curl_mime_name(part, "hardwareId");
        curl_mime_data(part, mac_addr, CURL_ZERO_TERMINATED);

        curl_easy_setopt(curl, CURLOPT_URL, UPLOAD_URL);
        curl_easy_setopt(curl, CURLOPT_MIMEPOST, mime);

        // Set write callback to capture response
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, (void *)&chunk);

        // Perform the request
        res = curl_easy_perform(curl);

        // Check for errors
        if (res != CURLE_OK)
        {
            fprintf(stderr, "curl_easy_perform() failed: %s\n",
                    curl_easy_strerror(res));
        }
        else
        {
            // Parse and print the response
            struct JsonResponse resp = parse_json_response(chunk.response);
            printf("Parsed response:\n");
            printf("Success: %s\n", resp.success ? "true" : "false");
            printf("Message: %s\n", resp.message);
            printf("Name of User: %s\n", resp.nameOfUser);

            // Open or close the lock based on the response
            if (resp.success)
            {
                // display_message_on_screen("Dupa!");
                open_lock(0); // Open the lock
                sleep(3);     // Wait for 2 seconds
                open_lock(1); // Close the lock
            }
            else
            {
                // display_message_on_screen(resp.message);
            }
        }

        // Clean up
        curl_mime_free(mime);
        curl_easy_cleanup(curl);
        free(chunk.response);
    }

    curl_global_cleanup();

    return 0;
}