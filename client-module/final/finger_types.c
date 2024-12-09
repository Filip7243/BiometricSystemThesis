#include <stdlib.h>
#include <time.h>
#include "finger_types.h"

const char *finger_to_string(FingerType finger)
{
    switch (finger)
    {
    case THUMB:
        return "THUMB";
    case INDEX:
        return "INDEX";
    case MIDDLE:
        return "MIDDLE";
    default:
        return "UNKNOWN";
    }
}

FingerType get_random_finger()
{
    static int seeded = 0;
    if (!seeded)
    {
        srand(time(NULL));
        seeded = 1;
    }

    return (FingerType)((rand() % 3));
}