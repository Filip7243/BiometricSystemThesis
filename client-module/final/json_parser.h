#ifndef JSON_PARSER_H
#define JSON_PARSER_H

#include <stdbool.h>

/**
 * Struktura reprezentująca odpowiedź JSON o identyfikacji użytkownika.
 * @param success - flaga określająca, czy operacja identyfikacji zakończyła się sukcesem
 * @param message - komunikat zwrócony przez serwer
 * @param nameOfUser - nazwa użytkownika zwrócona przez serwer (jeśli system nie rozpoznał użytkownika to pole to będzie null)
 */
struct JsonResponse
{
    bool success;
    char message[256];
    char nameOfUser[256];
};

/**
 * Główna funkcja parsująca odpowiedź JSON.
 * @param json - ciąg znaków zawierający odpowiedź w formacie JSON
 * @return Struktura JsonResponse zawierająca zparsowaną odpowiedź
 */
struct JsonResponse parse_json_response(const char *json);

#endif // JSON_PARSER_H