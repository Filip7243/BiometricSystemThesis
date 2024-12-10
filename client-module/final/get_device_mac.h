#ifndef GET_DEVICE_MAC_H
#define GET_DEVICE_MAC_H

// [in] interface: The name of the network interface.
// [out] mac_address: The MAC address of the network interface.
void get_mac(const char *interface, char *mac_address);

#endif