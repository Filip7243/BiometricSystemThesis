#include <sys/ioctl.h>
#include <net/if.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

void get_mac(const char *interface, char *mac_address)
{
    int fd;           // Deskryptor gniazda
    struct ifreq ifr; // Struktura przechowująca dane o interfejsie sieciowym

    // Otwarcie gniazda (socket) dla komunikacji z interfejsem sieciowym
    fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (fd == -1)
    {
        perror("Socket error");
        return;
    }

    // Skopiowanie nazwy interfejsu (np. "wlan0") do struktury ifreq
    strncpy(ifr.ifr_name, interface, IFNAMSIZ - 1);
    ifr.ifr_name[IFNAMSIZ - 1] = '\0';

    // Wywołanie ioctl w celu uzyskania adresu sprzętowego (adresu MAC)
    if (ioctl(fd, SIOCGIFHWADDR, &ifr) == -1)
    {
        perror("ioctl error");
        close(fd);
        return;
    }

    // Konwersja adresu MAC do formatu czytelnego (szesnastkowego)
    unsigned char *hwaddr = (unsigned char *)ifr.ifr_hwaddr.sa_data;
    sprintf(mac_address, "%02X:%02X:%02X:%02X:%02X:%02X",
            hwaddr[0], hwaddr[1], hwaddr[2], hwaddr[3], hwaddr[4], hwaddr[5]);

    // Zamknięcie deskryptora po wykonaniu operacji
    close(fd);
}