#ifndef FINGER_TYPES_H
#define FINGER_TYPES_H

typedef enum
{
    THUMB = 0,
    INDEX,
    MIDDLE
} FingerType;

FingerType get_random_finger();
const char *finger_to_string(FingerType finger);

#endif