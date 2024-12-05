#include <json-c/json.h>
#include <curl/curl.h>
#include <openssl/ssl.h>
#include <openssl/err.h>

int send_file()
{
    // FILE *file;
    // unsigned char *buffer;
    // long file_size;

    // file = fopen("frame_Ex.bmp", "rb");
    // if (!file)
    // {
    //     perror("Error opening file\n");
    //     return -1;
    // }

    // fseek(file, 0, SEEK_END);
    // file_size = ftell(file);
    // fseek(file, 0, SEEK_SET);

    // buffer = (unsigned char *)malloc(file_size);
    // if (!buffer)
    // {
    //     perror("Error allocating memory\n");
    //     fclose(file);
    //     return -1;
    // }

    // size_t read_bytes = fread(buffer, 1, file_size, file);
    // if (read_bytes != file_size)
    // {
    //     perror("Error reading file\n");
    //     fclose(file);
    //     free(buffer);
    //     return -1;
    // }
    // fclose(file);

    // size_t base64_len;
    // char *base64_str = base64_encode(buffer, file_size, &base64_len);
    // free(buffer);

    // // SEND REQUEST

    // free(buffer);

    CURL *curl;
    CURLcode res;
    FILE *file;
    long file_size;
    char response[4096] = {0};

    SSL_library_init();
    OpenSSL_add_all_algorithms();
    SSL_load_error_strings();

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

        // URL for secure upload
        const char *url = "http://10.100.123.34:8080/api/v1/enrollments";

        // Create form
        struct curl_httppost *formpost = NULL;
        struct curl_httppost *lastptr = NULL;
        struct curl_slist *headerlist = NULL;

        // Add file to form
        curl_formadd(&formpost, &lastptr,
                     CURLFORM_COPYNAME, "file",
                     CURLFORM_FILE, "frame_Ex.bmp",
                     CURLFORM_CONTENTTYPE, "image/bmp",
                     CURLFORM_END);

        curl_formadd(&formpost, &lastptr,
                     CURLFORM_COPYNAME, "type",
                     CURLFORM_COPYCONTENTS, "INDEX",
                     CURLFORM_END);

        curl_formadd(&formpost, &lastptr,
                     CURLFORM_COPYNAME, "id",
                     CURLFORM_COPYCONTENTS, "12345",
                     CURLFORM_END);

        curl_easy_setopt(curl, CURLOPT_URL, url);
        curl_easy_setopt(curl, CURLOPT_HTTPPOST, formpost);

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
        curl_formfree(formpost);
        curl_slist_free_all(headerlist);
        curl_easy_cleanup(curl);
        fclose(file);
    }

    EVP_cleanup();
    ERR_free_strings();

    curl_global_cleanup();

    return 0;
}

int main()
{
    send_file();
}