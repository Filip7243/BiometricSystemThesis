#include <curl/curl.h>

int send_file()
{
    CURL *curl;
    CURLcode res;
    FILE *file;
    long file_size;
    char response[4096] = {0};

    curl_global_init(CURL_GLOBAL_ALL);
    curl = curl_easy_init();

    if (curl)
    {
        // Open the file to upload
        file = fopen("frame_Ex.bmp", "rb");
        if (file == NULL)
        {
            fprintf(stderr, "Cannot open file\n");
            return 1;
        }

        // Get file size
        fseek(file, 0, SEEK_END);
        file_size = ftell(file);
        rewind(file);

        printf("File size: %d\n", file_size);

        // URL for secure upload
        const char *url = "http://192.168.88.87:8080/api/v1/enrollments";

        // Create form
        curl_mime *mime;
        curl_mimepart *part;

        mime = curl_mime_init(curl);
        part = curl_mime_addpart(mime);

        curl_mime_name(part, "file");
        curl_mime_filedata(part, "frame_Ex.bmp");
        curl_mime_type(part, "image/bmp");

        part = curl_mime_addpart(mime);
        curl_mime_name(part, "type");
        curl_mime_data(part, "INDEX", CURL_ZERO_TERMINATED);

        part = curl_mime_addpart(mime);
        curl_mime_name(part, "hardwareId");
        curl_mime_data(part, "12345", CURL_ZERO_TERMINATED);

        curl_easy_setopt(curl, CURLOPT_URL, url);
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
        fclose(file);
    }

    curl_global_cleanup();

    return 0;
}

int main()
{
    send_file();
}