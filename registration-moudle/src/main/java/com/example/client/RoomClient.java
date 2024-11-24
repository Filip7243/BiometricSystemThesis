package com.example.client;

import com.example.client.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RoomClient {

    private final HttpClient httpClient;

    public RoomClient() {
        this.httpClient = MyHttpClient.getInstance();
    }

    public void updateRoomWithId(Long id, UpdateRoomRequest updateRoomRequest) {
        try {
            HttpRequest request = createUpdateRoomWithIdRequest(id, updateRoomRequest);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to update building. Status code: " + response.statusCode());
            }

            System.out.println("Room update success!");
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to update building");
            throw new RuntimeException(e);
        }
    }

    public void deleteRoomWithId(Long id) {
        try {
            HttpRequest request = createDeleteRoomWithIdRequest(id);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {  // TODO: change to 204
                throw new IOException("Failed to delete room. Status code: " + response.statusCode());
            }

            System.out.println("Room delete success!");
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to delete room");
            throw new RuntimeException(e);
        }
    }

    public void removeDeviceFromRoom(AssignDeviceToRoomRequest assignDeviceToRoomRequest) {
        try {
            HttpRequest request = createRemoveDeviceFromRoomRequest(assignDeviceToRoomRequest);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to remove device from room. Status code: " + response.statusCode());
            }

            System.out.println("Device removed from room!");
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to remove device from room");
            throw new RuntimeException(e);
        }
    }

    public void assignDeviceToRoom(AssignDeviceToRoomRequest assignDeviceToRoomRequest) {
        try {
            HttpRequest request = createAssignDeviceToRoomRequest(assignDeviceToRoomRequest);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to remove device from room. Status code: " + response.statusCode());
            }

            System.out.println("Device removed from room!");
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to remove device from room");
            throw new RuntimeException(e);
        }
    }

    public RoomDTO addRoom(AddRoomRequest addRoomRequest) {
        try {
            HttpRequest request = createAddRoomRequest(addRoomRequest);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                throw new IOException("Failed to add room. Status code: " + response.statusCode());
            }

            return MyObjectMapper.getInstance().readValue(response.body(), RoomDTO.class);
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to add room");
            throw new RuntimeException(e);
        }
    }

    private HttpRequest createDeleteRoomWithIdRequest(Long id) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/rooms/" + id))
                .DELETE()
                .build();
    }

    private static HttpRequest createUpdateRoomWithIdRequest(Long id, UpdateRoomRequest request) throws IOException {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/rooms/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(MyObjectMapper.getInstance().writeValueAsString(request)))
                .build();
    }

    private HttpRequest createRemoveDeviceFromRoomRequest(AssignDeviceToRoomRequest request) throws JsonProcessingException {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/rooms/" + request.roomId() + "/devices/remove"))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(MyObjectMapper.getInstance().writeValueAsString(request)))
                .build();
    }

    private HttpRequest createAssignDeviceToRoomRequest(AssignDeviceToRoomRequest request) throws JsonProcessingException {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/rooms/" + request.roomId() + "/devices/assign"))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(MyObjectMapper.getInstance().writeValueAsString(request)))
                .build();
    }

    private HttpRequest createAddRoomRequest(AddRoomRequest request) throws JsonProcessingException {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/rooms"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MyObjectMapper.getInstance().writeValueAsString(request)))
                .build();
    }
}