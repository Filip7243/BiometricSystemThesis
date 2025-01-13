#include <stdio.h>
#include <pigpio.h>
#include <unistd.h>
#include "devices_controller.h" // Include the header file for declarations

// Define GPIO pin for lock control
#define LOCK_PIN 16
#define BUZZER_PIN 12

// GPIO pins for the LCD screen
#define LCD_RS 5  // D8
#define LCD_E 3   // D9
#define LCD_D4 37 // D4
#define LCD_D5 35 // D5
#define LCD_D6 33 // D6
#define LCD_D7 31 // D7

// Function to open or close the lock
void open_lock(int state)
{
    // Set the GPIO pin for the lock as output
    gpioSetMode(LOCK_PIN, PI_OUTPUT);

    // Control the lock based on the provided state
    if (state == 0) // Low state (open the lock)
    {
        gpioWrite(LOCK_PIN, 1);   // Set pin to low (lock open)
        gpioWrite(BUZZER_PIN, 1); // Set pin to low (lock open)
        printf("Lock opened\n");
    }
    else if (state == 1) // High state (close the lock)
    {
        gpioWrite(LOCK_PIN, 0);   // Set pin to high (lock closed)
        gpioWrite(BUZZER_PIN, 0); // Set pin to low (lock open)
        printf("Lock closed\n");
    }
    else
    {
        printf("Invalid state. Use 0 to open and 1 to close the lock.\n");
    }
}

// Funkcja do wysyłania pół-bajtów (4-bit mode)
void lcd_send_nibble(int nibble)
{
    gpioWrite(LCD_D4, nibble & 0x01);
    gpioWrite(LCD_D5, (nibble >> 1) & 0x01);
    gpioWrite(LCD_D6, (nibble >> 2) & 0x01);
    gpioWrite(LCD_D7, (nibble >> 3) & 0x01);

    gpioWrite(LCD_E, 1);
    usleep(1000); // Krótka przerwa
    gpioWrite(LCD_E, 0);
    usleep(1000);
}

// Funkcja wysyłająca bajt (dane/komenda)
void lcd_send_byte(int value, int mode)
{
    gpioWrite(LCD_RS, mode); // 0 = komenda, 1 = dane
    lcd_send_nibble(value >> 4);
    lcd_send_nibble(value & 0x0F);
    usleep(2000); // Czekaj na wykonanie
}

void lcd_init()
{
    // Initialize GPIO
    // if (gpioInitialise() < 0)
    // {
    //     fprintf(stderr, "pigpio initialization failed\n");
    //     return;
    // }

    // Set pins as outputs
    gpioSetMode(LCD_RS, PI_OUTPUT);
    gpioSetMode(LCD_E, PI_OUTPUT);
    gpioSetMode(LCD_D4, PI_OUTPUT);
    gpioSetMode(LCD_D5, PI_OUTPUT);
    gpioSetMode(LCD_D6, PI_OUTPUT);
    gpioSetMode(LCD_D7, PI_OUTPUT);

    // LCD initialization sequence
    usleep(50000);
    lcd_send_nibble(0x03);
    usleep(4500);
    lcd_send_nibble(0x03);
    usleep(4500);
    lcd_send_nibble(0x03);
    usleep(150);
    lcd_send_nibble(0x02);

    // Function set: 4-bit mode, 2 lines, 5x8 dots
    lcd_send_byte(0x28, 0);
    // Display control: display on, cursor off, blink off
    lcd_send_byte(0x0C, 0);
    // Clear display
    lcd_send_byte(0x01, 0);
    usleep(2000);
    // Entry mode set: increment cursor, no display shift
    lcd_send_byte(0x06, 0);
}

void lcd_clear()
{
    lcd_send_byte(0x01, 0);
    usleep(2000);
}

void lcd_setCursor(int row, int col)
{
    int row_offsets[] = {0x00, 0x40};
    lcd_send_byte(0x80 | (col + row_offsets[row]), 0);
}

void display_message_on_screen(const char *message)
{
    printf("Displaying message: %s\n", message);
    while (*message)
    {
        lcd_send_byte(*message++, 1);
    }
}