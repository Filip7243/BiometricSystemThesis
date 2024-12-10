#include <sys/ioctl.h>
#include <net/if.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>

void get_mac(const char *interface, char *mac_address)
{
    int fd;
    struct ifreq ifr;

    // Open a socket
    fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (fd == -1)
    {
        perror("Socket error");
        return;
    }

    // Copy the interface name (e.g., "eth0" or "wlan0") to the ifreq structure
    strncpy(ifr.ifr_name, interface, IFNAMSIZ - 1);
    ifr.ifr_name[IFNAMSIZ - 1] = '\0';

    // Perform an ioctl to get the hardware address
    if (ioctl(fd, SIOCGIFHWADDR, &ifr) == -1)
    {
        perror("ioctl error");
        close(fd);
        return;
    }

    // Convert the MAC address to a readable format
    unsigned char *hwaddr = (unsigned char *)ifr.ifr_hwaddr.sa_data;
    sprintf(mac_address, "%02X:%02X:%02X:%02X:%02X:%02X",
            hwaddr[0], hwaddr[1], hwaddr[2], hwaddr[3], hwaddr[4], hwaddr[5]);

    // Close the socket
    close(fd);
}