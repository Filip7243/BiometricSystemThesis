#ifndef GET_DEVICE_MAC_H
#define GET_DEVICE_MAC_H

/**
 * Funkcja odczytuje adres MAC z interfejsu sieciowego o nazwie podanej w argumencie `interface`
 * i zapisuje go w zmiennej `mac_address`. Adres MAC jest zapisywany w formacie szesnastkowym
 * (na przykład: "00:1A:2B:3C:4D:5E"). Dzięki temu system może zidentyfikować pomieszczenie do którego użytkownik
 * próbuje się dostać.
 *
 * @param interface Nazwa interfejsu sieciowego, z którego ma zostać pobrany adres MAC (np. "wlan0").
 * @param mac_address Wskaźnik na bufor, w którym zostanie zapisany adres MAC w formacie szesnastkowym.
 */
void get_mac(const char *interface, char *mac_address);

#endif