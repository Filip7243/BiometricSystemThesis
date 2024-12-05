TARGET=ftrScanAPI_Ex_test

CFLAGS=-Wall -O2 -I./       

FTRSCANAPI_DLIB=libScanAPI.so -lm -lusb
LIBS=-lcurl -ljson-c $(FTRSCANAPI_DLIB)

all: $(TARGET)

$(TARGET): $(TARGET).c
	$(CC) $(CFLAGS) -o $(TARGET) $(TARGET).c $(LIBS)

clean:
	rm -f $(TARGET)
