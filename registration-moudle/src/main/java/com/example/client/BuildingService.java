package com.example.client;

import com.example.client.dto.BuildingDTO;
import com.example.client.dto.CreateBuildingRequest;
import com.example.client.dto.UserDTO;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class BuildingService {

    private final BuildingClient buildingClient;

    public BuildingService(BuildingClient buildingClient) {
        this.buildingClient = buildingClient;
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
                             Consumer<CreateBuildingRequest> onSuccess,
                             Component parentComponent) {
        BaseResourceWorker.execute(
                () -> buildingClient.saveBuilding(request),
                onSuccess,
                parentComponent
        );
    }
}
