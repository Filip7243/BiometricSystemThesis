#ifndef DEVICES_CONTROLLER_H
#define DEVICES_CONTROLLER_H

#include <stdbool.h>

void open_lock(int state);
void display_message_on_screen(const char *message);

#endif // DEVICES_CONTROLLER_H