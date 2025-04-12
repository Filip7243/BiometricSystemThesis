TARGET=scan_finger

CFLAGS=-Wall -O2 -I./ -I/usr/include

FTRSCANAPI_DLIB=libScanAPI.so -lm -lusb 
LIBS=-lcurl -lpigpio -lrt -lcrypto $(FTRSCANAPI_DLIB)

all: $(TARGET)

$(TARGET): $(TARGET).c
	$(CC) $(CFLAGS) -o $(TARGET) finger_types.c send.c get_device_mac.c json_parser.c devices_controller.c data_encryption.c $(TARGET).c $(LIBS)

clean:
	rm -f $(TARGET)