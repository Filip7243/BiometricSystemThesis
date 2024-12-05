#include <json-c/json.h>
#include <curl/curl.h>

int main()
{
    CURL *curl;
    CURLcode res;
    struct curl_slist *headers = NULL; // Request headers

    // Create JSON object to send to server
    json_object *json_obj = json_object_new_object();
    json_object *token = json_object_new_array(); // Fingerprint bytes

    for (int i = 0; i < 100; i++)
    {
        json_object_array_add(token, json_object_new_int(0));
    }

    json_object_object_add(json_obj, "token", token);
    json_object_object_add(json_obj, "type", json_object_new_string("INDEX"));               // TODO: add logic to get random finger type
    json_object_object_add(json_obj, "hardware_id", json_object_new_string("MY_HARDWARE!")); // TODO: add logic to get hardware id

    const char *json_str = json_object_to_json_string(json_obj);

    curl_global_init(CURL_GLOBAL_ALL);
    curl = curl_easy_init();

    if (curl)
    {
        curl_easy_setopt(curl, CURLOPT_URL, "http://10.100.123.34:8080/api/v1/enrollments");
        headers = curl_slist_append(headers, "Content-Type: application/json");

        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, json_str);

        res = curl_easy_perform(curl);

        if (res != CURLE_OK)
        {
            fprintf(stderr, "curl_easy_perform() failed: %s\n", curl_easy_strerror(res));
            curl_easy_cleanup(curl);
            curl_slist_free_all(headers);
            json_object_put(json_obj);

            return -1;
        }

        curl_easy_cleanup(curl);
        curl_slist_free_all(headers);

        json_object_put(json_obj);
        curl_global_cleanup();

        return 0;
    }
}