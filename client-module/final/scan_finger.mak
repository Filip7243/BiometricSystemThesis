TARGET=scan_finger

CFLAGS=-Wall -O2 -I./      

FTRSCANAPI_DLIB=libScanAPI.so -lm -lusb
LIBS=-lcurl $(FTRSCANAPI_DLIB)

all: $(TARGET)

$(TARGET): $(TARGET).c
	$(CC) $(CFLAGS) -o $(TARGET) finger_types.c send.c get_device_mac.c $(TARGET).c $(LIBS)

clean:
	rm -f $(TARGET)