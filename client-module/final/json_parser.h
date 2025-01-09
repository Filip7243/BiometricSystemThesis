#ifndef JSON_PARSER_H
#define JSON_PARSER_H

#include <stdbool.h>

struct JsonResponse {
    bool success;
    char message[256];
    char nameOfUser[256];
};

// Główna funkcja parsująca
struct JsonResponse parse_json_response(const char* json);

#endif // JSON_PARSER_H