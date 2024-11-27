package com.example.client;

import java.awt.*;
import java.util.function.Consumer;

public class RoomService {

    private final RoomClient roomClient;

    public RoomService(RoomClient roomClient) {
        this.roomClient = roomClient;
    }

    public void assignRoomToUser(Long roomId, Long userId, Consumer<Void> onSuccess, Component parentComponent) {
        BaseResourceWorker.execute(
                () -> {
                    roomClient.assignRoomToUser(roomId, userId);
                    return null;
                },
                onSuccess,
                parentComponent
        );
    }
}
