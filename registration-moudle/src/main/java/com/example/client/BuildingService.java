package com.example.client;

import com.example.client.dto.*;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class BuildingService {

    private final BuildingClient buildingClient;
    private final RoomClient roomClient;

    public BuildingService(BuildingClient buildingClient, RoomClient roomClient) {
        this.buildingClient = buildingClient;
        this.roomClient = roomClient;
    }

    public void getAllBuildingsNotAssignedToUser(Long userId, Consumer<List<BuildingDTO>> onSuccess, Component parentComponent) {
        BaseResourceWorker.execute(
                () -> buildingClient.getAllBuildingsNotAssignedToUser(userId),
                onSuccess,
                parentComponent
        );
    }

    public void getAllBuildings(Consumer<List<BuildingDTO>> onSuccess, Component parentComponent) {
        BaseResourceWorker.execute(
                buildingClient::getAllBuildings,
                onSuccess,
                parentComponent
        );
    }

    public void saveBuilding(CreateBuildingRequest request,
                             Consumer<BuildingDTO> onSuccess,
                             Component parentComponent) {
        BaseResourceWorker.execute(
                () -> buildingClient.saveBuilding(request),
                onSuccess,
                parentComponent
        );
    }

    public void updateBuildingWithId(Long buildingId, UpdateBuildingRequest request,
                               Consumer<Void> onSuccess,
                               Component parentComponent) {
        BaseResourceWorker.execute(
                () -> {
                    buildingClient.updateBuildingWithId(buildingId, request);
                    return null;
                },
                onSuccess,
                parentComponent
        );
    }

    public void deleteBuildingWithId(Long buildingId,
                               Consumer<Void> onSuccess,
                               Component parentComponent) {
        BaseResourceWorker.execute(
                () -> {
                    buildingClient.deleteBuildingWithId(buildingId);
                    return null;
                },
                onSuccess,
                parentComponent
        );
    }

    public void addRoomToBuilding(AddRoomRequest request,
                                  Consumer<RoomDTO> onSuccess,
                                  Component parentComponent) {
        BaseResourceWorker.execute(
                () -> roomClient.addRoom(request),
                onSuccess,
                parentComponent
        );
    }

    public void getBuildingById(Long buildingId,
                                Consumer<BuildingDTO> onSuccess,
                                Component parentComponent) {
        BaseResourceWorker.execute(
                () -> buildingClient.getBuildingById(buildingId),
                onSuccess,
                parentComponent
        );
    }
}
