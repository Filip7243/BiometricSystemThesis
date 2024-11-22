package com.example.client;

import com.example.client.dto.BuildingDTO;
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

    public void getAllBuildingsAsync(Consumer<List<BuildingDTO>> onSuccess, Consumer<Exception> onError) {
        SwingWorker<List<BuildingDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BuildingDTO> doInBackground() throws Exception {
                HttpRequest request = createGetAllBuildingsRequest();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new IOException("Failed to retrieve buildings. Status code: " + response.statusCode());
                }

                return objectMapper.readValue(
                        response.body(),
                        new TypeReference<>() {
                        }
                );
            }

            @Override
            protected void done() {
                try {
                    List<BuildingDTO> buildings = get();
                    onSuccess.accept(buildings);
                } catch (Exception e) {
                    onError.accept(e);
                }
            }
        };

        worker.execute();
    }

    private static HttpRequest createGetAllBuildingsRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/buildings"))
                .header("Accept", "application/json")
                .GET()
                .build();
    }
}
