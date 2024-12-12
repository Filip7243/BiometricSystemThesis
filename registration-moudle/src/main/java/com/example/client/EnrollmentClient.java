package com.example.client;

import com.example.client.dto.LateControlDTO;
import com.example.client.dto.RoomEntranceDTO;
import com.example.client.dto.UnconfirmedEntranceDTO;
import com.example.client.dto.UserEnrollmentConfirmationDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

public class EnrollmentClient {

    public List<RoomEntranceDTO> getNumberOfEntrancesToEachRoomOnDate(LocalDate date, Long buildingId) {
        try {
            System.out.println("DATA: " + date.toString());
            String urlString = String.format(
                    "http://localhost:8080/api/v1/enrollments/entrances-to-room?date=%s&buildingId=%d",
                    date.toString(),
                    buildingId
            );
            System.out.println("URL: " + urlString);
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

    public List<UnconfirmedEntranceDTO> getUnconfirmedEntrancesPerUserByRoom() {
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

    public List<UserEnrollmentConfirmationDTO> getUserEnrollmentConfirmationRate(Long userId) {
        try {
            String urlString = String.format(
                    "http://localhost:8080/api/v1/enrollments/enrollments-confirmation-rate?&userId=%d",
                    userId
            );

            System.out.println("URL Hourly Enrollments: " + urlString);
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

    public List<LateControlDTO> getLateControlByUserAndRoom(LocalDate date, Long userId, int expectedHour) {
        try {
            String urlString = String.format(
                    "http://localhost:8080/api/v1/enrollments/late-control?date=%s&userId=%d&expectedHour=%d",
                    date.toString(),
                    userId,
                    expectedHour
            );

            System.out.println("LATE CONTORL URL: " + urlString);

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
