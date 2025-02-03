package com.example.client;

import com.example.client.dto.DeviceDTO;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class DeviceService {

    private final DeviceClient deviceClient;

    public DeviceService(DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
    }

    public void getDevicesNotAssignedToRoom(Consumer<List<DeviceDTO>> onSuccess, Component parentComponent) {
        BaseResourceWorker.execute(
                deviceClient::getDevicesNotAssignedToRoom,
                onSuccess,
                parentComponent
        );
    }
}
