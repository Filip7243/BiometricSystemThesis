#include <curl/curl.h>
#include <finger_types.h>
#include <send.h>

int send_file(FingerType type)
{
    CURL *curl;
    CURLcode res;
    char response[4096] = {0};

    curl_global_init(CURL_GLOBAL_ALL);
    curl = curl_easy_init();

    if (curl)
    {

        // Create form
        curl_mime *mime;
        curl_mimepart *part;

        mime = curl_mime_init(curl);
        part = curl_mime_addpart(mime);

        curl_mime_filedata(part, IMAGE_NAME);
        curl_mime_name(part, "file");
        curl_mime_type(part, IMAGE_TYPE);

        part = curl_mime_addpart(mime);
        curl_mime_name(part, "type");
        curl_mime_data(part, finger_to_string(type), CURL_ZERO_TERMINATED);

        part = curl_mime_addpart(mime);
        curl_mime_name(part, "hardwareId");
        curl_mime_data(part, "12345", CURL_ZERO_TERMINATED);

        curl_easy_setopt(curl, CURLOPT_URL, UPLOAD_URL);
        curl_easy_setopt(curl, CURLOPT_MIMEPOST, mime);

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
            printf("Response: %s\n", response);
        }

        // Clean up
        curl_mime_free(mime);
        curl_easy_cleanup(curl);
    }

    curl_global_cleanup();

    return 0;
}