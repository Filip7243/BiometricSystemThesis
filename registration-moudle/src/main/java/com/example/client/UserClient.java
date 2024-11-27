package com.example.client;

import com.example.client.dto.RoomDTO;
import com.example.client.dto.UpdateFingerprintRequest;
import com.example.client.dto.UpdateUserRequest;
import com.example.client.dto.UserDTO;
import com.example.client.request.UserCreationRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static com.example.model.FingerType.*;

public final class UserClient {

    private static final String USER_API_URL = "http://localhost:8080/api/v1/users";

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

    public List<UserDTO> getAllUsers() {
        try {
            HttpRequest request = createGetAllUsersRequest();

            HttpResponse<String> response = MyHttpClient.getInstance().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to retrieve users. Status code: " + response.statusCode());
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

    public void updateUser(UpdateUserRequest request) {
        try {
            HttpRequest updateRequest = createUpdateUserRequest(request);

            HttpResponse<String> response = MyHttpClient.getInstance().send(updateRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to update user. Status code: " + response.statusCode());
            }

            System.out.println("User updated!");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUser(Long id) {
        try {
            HttpRequest deleteRequest = createDeleteUserWithIdRequest(id);

            HttpResponse<String> response = MyHttpClient.getInstance().send(deleteRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 204) {
                throw new IOException("Failed to delete user. Status code: " + response.statusCode());
            }

            System.out.println("User deleted!");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateFingerprint(UpdateFingerprintRequest updateFingerprintRequest) {
        try {
            HttpRequest request = createUpdateFingerprintRequest(updateFingerprintRequest);

            HttpResponse<String> response = MyHttpClient.getInstance().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to update fingerprint. Status code: " + response.statusCode());
            }

            System.out.println("Fingerprint updated!");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<RoomDTO> getUserRooms(Long userId) {
        try {
            HttpRequest request = createGetUserRoomsRequest(userId);

            HttpResponse<String> response = MyHttpClient.getInstance().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to retrieve user rooms. Status code: " + response.statusCode());
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

    public void detachUserFromRoom(Long userId, Long roomId) {
        try {
            HttpRequest request = createDetachUserFromRoomRequest(userId, roomId);

            HttpResponse<String> response = MyHttpClient.getInstance().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to detach user from room. Status code: " + response.statusCode());
            }

            System.out.println("User detached from room!");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpRequest createGetAllUsersRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/users"))
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    private static HttpRequest createUpdateUserRequest(UpdateUserRequest request) throws IOException {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/users"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(MyObjectMapper.getInstance().writeValueAsString(request)))
                .build();
    }

    private static HttpRequest createDeleteUserWithIdRequest(Long id) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/users/" + id))
                .DELETE()
                .build();
    }

    private static HttpRequest createUpdateFingerprintRequest(UpdateFingerprintRequest request) throws IOException {
        System.out.println("UPDATE FINGERPRINT REQUEST: " + request);
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/fingerprints"))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(MyObjectMapper.getInstance().writeValueAsString(request)))
                .build();
    }

    private static HttpRequest createGetUserRoomsRequest(Long userId) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/users/" + userId + "/rooms"))
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    private static HttpRequest createDetachUserFromRoomRequest(Long userId, Long roomId) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/users/" + userId + "/rooms/" + roomId))
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();
    }

}
