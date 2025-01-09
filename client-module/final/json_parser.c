#include "json_parser.h"
#include <string.h>
#include <stdio.h>

struct JsonResponse parse_json_response(const char* json) {
    struct JsonResponse result = {false, "", ""};
    
    // Parse success
    const char* success_pattern = "\"success\":";
    char* pos = strstr(json, success_pattern);
    if (pos != NULL) {
        pos += strlen(success_pattern);
        while (*pos == ' ' || *pos == '\t') pos++;
        result.success = (strncmp(pos, "true", 4) == 0);
    }
    
    // Parse message
    const char* message_pattern = "\"message\":\"";
    pos = strstr(json, message_pattern);
    if (pos != NULL) {
        pos += strlen(message_pattern);
        int i = 0;
        while (*pos != '"' && i < 255) {
            result.message[i++] = *pos++;
        }
        result.message[i] = '\0';
    }
    
    // Parse nameOfUser
    const char* name_pattern = "\"nameOfUser\":\"";
    pos = strstr(json, name_pattern);
    if (pos != NULL) {
        pos += strlen(name_pattern);
        int i = 0;
        while (*pos != '"' && i < 255) {
            result.nameOfUser[i++] = *pos++;
        }
        result.nameOfUser[i] = '\0';
    }
    
    return result;
}