package com.example.client;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

public class EnrollmentClient {

    public List<Object[]> getNumberOfEntrancesToEachRoomOnDate(LocalDate date, Long buildingId) {
        try {
            String urlString = String.format(
                    "http://localhost:8080/api/v1/enrollments/late-control?date=%s&buildingId=%d",
                    date.toString(),
                    buildingId
            );
            HttpRequest request = createGetRequest(urlString);

            HttpResponse<String> response = MyHttpClient.getInstance().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to retrieve entrances data. Status code: " + response.statusCode());
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

    public List<Object[]> getUnconfirmedEntrancesPerUserByRoom() {
        try {
            HttpRequest request = createGetRequest("http://localhost:8080/api/v1/enrollments/unconfirmed-entrances-per-user-by-room");

            HttpResponse<String> response = MyHttpClient.getInstance().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to retrieve entrances data. Status code: " + response.statusCode());
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

    public List<Object[]> getUserEnrollmentConfirmationRate(Long userId) {
        try {
            String urlString = String.format(
                    "http://localhost:8080/api/v1/enrollments/late-control?&userId=%d",
                    userId
            );
            HttpRequest request = createGetRequest(urlString);

            HttpResponse<String> response = MyHttpClient.getInstance().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to retrieve entrances data. Status code: " + response.statusCode());
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

    public List<Object[]> getLateControlByUserAndRoom(LocalDate date, Long userId, int expectedHour) {
        try {
            String urlString = String.format(
                    "http://localhost:8080/api/v1/enrollments/late-control?date=%s&userId=%d&expectedHour=%d",
                    date.toString(),
                    userId,
                    expectedHour
            );

            HttpRequest request = createGetRequest(urlString);

            HttpResponse<String> response = MyHttpClient.getInstance().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to retrieve entrances data. Status code: " + response.statusCode());
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


    private static HttpRequest createGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
    }
}
