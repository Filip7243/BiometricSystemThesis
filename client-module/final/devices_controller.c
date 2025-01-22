#include <stdio.h>
#include <pigpio.h>
#include <unistd.h>
#include "devices_controller.h" // Include the header file for declarations

void init_lock_and_buzzer()
{
    // Set the GPIO pin for the lock as output
    gpioSetMode(LOCK_PIN, PI_OUTPUT);
    gpioSetMode(BUZZER_PIN, PI_OUTPUT);
}

// Function to open or close the lock
void open_lock(int state)
{
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

void lcd_toggle_enable()
{
    gpioWrite(LCD_E, 1);
    usleep(E_PULSE);
    gpioWrite(LCD_E, 0);
    usleep(E_DELAY);
}

void lcd_byte(int bits, int mode)
{
    // Set RS pin mode (command/data)
    gpioWrite(LCD_RS, mode);

    // High bits
    gpioWrite(LCD_D4, (bits & 0x10) >> 4);
    gpioWrite(LCD_D5, (bits & 0x20) >> 5);
    gpioWrite(LCD_D6, (bits & 0x40) >> 6);
    gpioWrite(LCD_D7, (bits & 0x80) >> 7);

    // Toggle enable pin
    lcd_toggle_enable();

    // Low bits
    gpioWrite(LCD_D4, bits & 0x01);
    gpioWrite(LCD_D5, (bits & 0x02) >> 1);
    gpioWrite(LCD_D6, (bits & 0x04) >> 2);
    gpioWrite(LCD_D7, (bits & 0x08) >> 3);

    // Toggle enable pin
    lcd_toggle_enable();
}

void lcd_init()
{
    // Set GPIO pins as output
    gpioSetMode(LCD_RS, PI_OUTPUT);
    gpioSetMode(LCD_E, PI_OUTPUT);
    gpioSetMode(LCD_D4, PI_OUTPUT);
    gpioSetMode(LCD_D5, PI_OUTPUT);
    gpioSetMode(LCD_D6, PI_OUTPUT);
    gpioSetMode(LCD_D7, PI_OUTPUT);

    lcd_byte(0x33, LCD_CMD); // Initialize
    lcd_byte(0x32, LCD_CMD); // Initialize
    lcd_byte(0x28, LCD_CMD); // 2 line 5x7 matrix
    lcd_byte(0x0C, LCD_CMD); // Turn cursor off
    lcd_byte(0x06, LCD_CMD); // Shift cursor right
    lcd_byte(0x01, LCD_CMD); // Clear display
    usleep(E_DELAY);
}

void lcd_string(const char *message, int line)
{
    lcd_byte(line, LCD_CMD);

    for (int i = 0; i < LCD_WIDTH; i++)
    {
        if (message[i] != '\0')
        {
            lcd_byte(message[i], LCD_CHR);
        }
        else
        {
            lcd_byte(' ', LCD_CHR); // Pad with spaces
        }
    }
}