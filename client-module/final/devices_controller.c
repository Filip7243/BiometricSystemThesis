#include <stdio.h>
#include <pigpio.h>
#include <unistd.h>
#include "devices_controller.h" // Include the header file for declarations

// Define GPIO pin for lock control
#define LOCK_PIN 16
#define BUZZER_PIN 12

// GPIO pins for the LCD screen
#define LCD_RS 3  // D9
#define LCD_E 5   // D8
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