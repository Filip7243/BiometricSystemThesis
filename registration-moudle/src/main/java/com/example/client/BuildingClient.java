package com.example.client;

import com.example.client.dto.BuildingDTO;
import com.example.client.dto.CreateBuildingRequest;
import com.example.client.dto.UpdateBuildingRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.function.Consumer;

public class BuildingClient {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BuildingClient() {
        this.httpClient = MyHttpClient.getInstance();
        this.objectMapper = MyObjectMapper.getInstance();
    }

    public List<BuildingDTO> getAllBuildings() {
        try {
            HttpRequest request = createGetAllBuildingsRequest();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to retrieve buildings. Status code: " + response.statusCode());
            }

            System.out.println("DUPA" + response.body());

            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<>() {
                    }
            );
        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);  // TODO: handling
            return List.of();
        }
    }

    public void updateBuildingWithId(Long id, UpdateBuildingRequest updateBuildingRequest) {
        try {
            HttpRequest request = createUpdateBuildingWithIdRequest(id, updateBuildingRequest);
            System.out.println(request);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to update building. Status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to update building");
            throw new RuntimeException(e);
        }
    }

    public void deleteBuildingWithId(Long id) {
        try {
            HttpRequest request = createDeleteBuildingWithIdRequest(id);
            System.out.println(request);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 204) {  // TODO: do 204
                throw new IOException("Failed to delete building. Status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to delete building");
            throw new RuntimeException(e);
        }
    }

    public BuildingDTO getBuildingById(Long id) {
        try {
            HttpRequest request = createGetBuildingByIdRequest(id);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to retrieve building. Status code: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), BuildingDTO.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: add BASIC URL CONST

    public List<BuildingDTO> getAllBuildingsNotAssignedToUser(Long id) {
        try {
            System.out.println("ID: " + id);
            HttpRequest request = createGetAllBuildingsNotAssignedToUserRequest(id);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to retrieve buildings. Status code: " + response.statusCode());
            }

            System.out.println("HERE: " + response.body());
            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<>() {
                    }
            );
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public BuildingDTO saveBuilding(CreateBuildingRequest createBuildingRequest) {
        try {
            HttpRequest request = createAddBuildingRequest(createBuildingRequest);
            System.out.println(request);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                throw new IOException("Failed to create building. Status code: " + response.statusCode());
            }

            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<>() {
                    }
            );
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to create building");
            throw new RuntimeException(e);
        }
    }

    private static HttpRequest createGetAllBuildingsRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/buildings"))
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    private static HttpRequest createUpdateBuildingWithIdRequest(Long id, UpdateBuildingRequest request) throws IOException {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/buildings/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(MyObjectMapper.getInstance().writeValueAsString(request)))
                .build();
    }

    private static HttpRequest createDeleteBuildingWithIdRequest(Long id) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/buildings/" + id))
                .DELETE()
                .build();
    }

    private static HttpRequest createGetBuildingByIdRequest(Long id) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/buildings/" + id))
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    private static HttpRequest createGetAllBuildingsNotAssignedToUserRequest(Long id) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/buildings/rooms/not-assigned/" + id))
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    private static HttpRequest createAddBuildingRequest(CreateBuildingRequest request) throws IOException {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/buildings"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MyObjectMapper.getInstance().writeValueAsString(request)))
                .build();
    }
}
