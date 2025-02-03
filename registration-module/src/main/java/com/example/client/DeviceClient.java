package com.example.client;

import com.example.client.dto.DeviceDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class DeviceClient {

    public List<DeviceDTO> getDevicesNotAssignedToRoom() {
        try {
            HttpRequest request = createGetDevicesNotAssignedToRoomRequest();

            HttpResponse<String> response = MyHttpClient.getInstance().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to retrieve user. Status code: " + response.statusCode());
            }

            return MyObjectMapper.getInstance().readValue(
                    response.body(),
                    new TypeReference<>() {
                    }
            );
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpRequest createGetDevicesNotAssignedToRoomRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/devices/not-assigned"))
                .header("Accept", "application/json")
                .GET()
                .build();
    }
}
