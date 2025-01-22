#ifndef DEVICES_CONTROLLER_H
#define DEVICES_CONTROLLER_H

#include <stdbool.h>

// Define GPIO pin for lock control
#define LOCK_PIN 16
#define BUZZER_PIN 12

// Consts for LCD
#define LCD_RS 3  // D8
#define LCD_E 2   // D9
#define LCD_D4 26 // D4
#define LCD_D5 19 // D5
#define LCD_D6 13 // D6
#define LCD_D7 6  // D7

#define LCD_WIDTH 16    // Maximum characters per line
#define LCD_CHR 1       // Mode - Sending data
#define LCD_CMD 0       // Mode - Sending command
#define LCD_LINE_1 0x80 // LCD RAM address for the 1st line
#define LCD_LINE_2 0xC0 // LCD RAM address for the 2nd line

#define E_PULSE 500
#define E_DELAY 500

void init_lock_and_buzzer();
void open_lock(int state);
void lcd_init();
void lcd_string(const char *message, int line);

#endif // DEVICES_CONTROLLER_H