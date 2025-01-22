#include "json_parser.h"
#include <string.h>
#include <stdio.h>

struct JsonResponse parse_json_response(const char *json)
{
    // Inicjalizacja struktury z domyślnymi wartościami
    struct JsonResponse result = {false, "", ""};

    // Parsowanie pola 'success' z JSON
    const char *success_pattern = "\"success\":";
    char *pos = strstr(json, success_pattern);
    if (pos != NULL)
    {
        pos += strlen(success_pattern);
        while (*pos == ' ' || *pos == '\t')
            pos++;
        result.success = (strncmp(pos, "true", 4) == 0);
    }

    // Parsowanie pola 'message' z JSON
    const char *message_pattern = "\"message\":\"";
    pos = strstr(json, message_pattern);
    if (pos != NULL)
    {
        pos += strlen(message_pattern);
        int i = 0;
        // Kopiuje zawartość wiadomości do 'message' aż do napotkania cudzysłowu
        while (*pos != '"' && i < 255)
        {
            result.message[i++] = *pos++;
        }
        result.message[i] = '\0';
    }

    // Parsowanie pola 'nameOfUser' z JSON
    const char *name_pattern = "\"nameOfUser\":\"";
    pos = strstr(json, name_pattern);
    if (pos != NULL)
    {
        pos += strlen(name_pattern);
        int i = 0;
        // Kopiuje zawartość nazwy użytkownika do 'nameOfUser' aż do napotkania cudzysłowu
        while (*pos != '"' && i < 255)
        {
            result.nameOfUser[i++] = *pos++;
        }
        result.nameOfUser[i] = '\0';
    }

    return result;
}