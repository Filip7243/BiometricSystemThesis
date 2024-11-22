package com.example.client;

import com.example.client.request.UserCreationRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static com.example.model.FingerType.*;

public final class UserClient {

    public void createUser(UserCreationRequest request) {
        try {
            JSONObject fingerprintData = new JSONObject();

            fingerprintData.put("THUMB", request.fingerprintData().get(THUMB));
            fingerprintData.put("INDEX", request.fingerprintData().get(INDEX));
            fingerprintData.put("MIDDLE", request.fingerprintData().get(MIDDLE));

            JSONObject payload = new JSONObject();
            payload.put("firstName", request.firstName());
            payload.put("lastName", request.lastName());
            payload.put("pesel", request.pesel());
            payload.put("role", request.role().name());
            payload.put("fingerprintData", fingerprintData);

            JSONArray roomIds = new JSONArray();
            request.roomIds().forEach(roomIds::put);
            payload.put("roomIds", roomIds);

            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/v1/users"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();


            HttpResponse<String> response = MyHttpClient.getInstance().send(postRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
