package com.example.client;

import com.example.client.dto.AssignDeviceToRoomRequest;
import com.example.client.dto.RoomDTO;
import com.example.client.dto.UpdateRoomRequest;

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

    public void updateRoomWithId(Long id,
                                 UpdateRoomRequest request,
                                 Consumer<Void> onSuccess,
                                 Component parentComponent) {
        BaseResourceWorker.execute(
                () -> {
                    roomClient.updateRoomWithId(id, request);
                    return null;
                },
                onSuccess,
                parentComponent
        );
    }

    public void deleteRoomWithId(Long roomId,
                                 Consumer<Void> onSuccess,
                                 Component parentComponent) {
        BaseResourceWorker.execute(
                () -> {
                    roomClient.deleteRoomWithId(roomId);
                    return null;
                },
                onSuccess,
                parentComponent
        );
    }

    public void removeDeviceFromRoom(AssignDeviceToRoomRequest request,
                                     Consumer<Void> onSuccess,
                                     Component parentComponent) {
        BaseResourceWorker.execute(
                () -> {
                    roomClient.removeDeviceFromRoom(request);
                    return null;
                },
                onSuccess,
                parentComponent
        );
    }

    public void assignDeviceToRoom(AssignDeviceToRoomRequest request,
                                   Consumer<Void> onSuccess,
                                   Component parentComponent) {
        BaseResourceWorker.execute(
                () -> {
                    roomClient.assignDeviceToRoom(request);
                    return null;
                },
                onSuccess,
                parentComponent
        );
    }

    public void getRoomById(Long roomId,
                            Consumer<RoomDTO> onSuccess,
                            Component parentComponent) {
        BaseResourceWorker.execute(
                () -> roomClient.getRoomById(roomId),
                onSuccess,
                parentComponent
        );
    }
}
