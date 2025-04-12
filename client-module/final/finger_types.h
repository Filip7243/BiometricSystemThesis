#ifndef FINGER_TYPES_H
#define FINGER_TYPES_H

/**
 * Definiuje trzy rodzaje palców, które są dostępne w systemie:
 * - THUMB: Kciuk
 * - INDEX: Wskazujący
 * - MIDDLE: Środkowy
 */
typedef enum
{
    THUMB = 0,
    INDEX,
    MIDDLE
} FingerType;

/**
 * Funkcja losuje jeden z trzech dostępnych rodzajów palców (THUMB, INDEX, MIDDLE) i zwraca go.
 *
 * @return Zwraca losowy typ palca.
 */
FingerType get_random_finger();

/**
 * Funkcja zwraca tekstową reprezentację danego rodzaju palca aby można ją było wyświetlić na ekranie:
 * - "THUMB" dla kciuka
 * - "INDEX" dla wskazującego
 * - "MIDDLE" dla środkowego
 *
 * @param finger Typ palca do konwersji na tekst.
 * @return Zwraca tekstową reprezentację typu palca.
 */
const char *finger_to_string(FingerType finger);

#endif