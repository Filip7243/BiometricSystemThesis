package com.example.client;

import com.example.client.dto.*;
import com.example.client.request.UserCreationRequest;
import com.example.model.FingerType;
import com.example.model.Role;
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
            JSONObject fingerprintImageData = new JSONObject();
            fingerprintImageData.put("THUMB", request.fingerprintImageData().get(THUMB));
            fingerprintImageData.put("INDEX", request.fingerprintImageData().get(INDEX));
            fingerprintImageData.put("MIDDLE", request.fingerprintImageData().get(MIDDLE));

            JSONObject payload = new JSONObject();
            payload.put("firstName", request.firstName());
            payload.put("lastName", request.lastName());
            payload.put("pesel", request.pesel());
            payload.put("role", request.role().name());
            payload.put("fingerprintImageData", fingerprintImageData);

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

    public LoginResponse loginToAdminPanelWithBiometrics(BiometricsLoginRequest requestBody) {
        try {
            // Przygotowanie payload jako JSON
            JSONObject payload = new JSONObject();
            payload.put("file", requestBody.file());
            payload.put("type", requestBody.type());

            // Budowanie żądania HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/v1/enrollments/login-biometrics"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            // Wysyłanie żądania i odbieranie odpowiedzi
            HttpResponse<String> response = MyHttpClient.getInstance()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            // Obsługa odpowiedzi
            if (response.statusCode() == 200) {
                JSONObject responseBody = new JSONObject(response.body());
                Boolean isLoggedIn = responseBody.getBoolean("isLoggedIn");
                return new LoginResponse(isLoggedIn);
            } else {
                // Rzucenie wyjątku w przypadku błędnego statusu odpowiedzi
                throw new RuntimeException("Request failed with status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("An error occurred while sending the request.", e);
        }
    }

    public LoginResponse loginToAdminPanelWithPassword(PasswordLoginRequest requestBody) {
        try {
            // Przygotowanie payload jako JSON
            JSONObject payload = new JSONObject();
            payload.put("encryptedPassword", requestBody.encryptedPassword());

            // Budowanie żądania HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/v1/enrollments/login-password"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                    .build();

            // Wysyłanie żądania i odbieranie odpowiedzi
            HttpResponse<String> response = MyHttpClient.getInstance()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            // Obsługa odpowiedzi
            if (response.statusCode() == 200) {
                JSONObject responseBody = new JSONObject(response.body());
                Boolean isLoggedIn = responseBody.getBoolean("isLoggedIn");
                return new LoginResponse(isLoggedIn);
            } else {
                // Rzucenie wyjątku w przypadku błędnego statusu odpowiedzi
                throw new RuntimeException("Request failed with status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("An error occurred while sending the request.", e);
        }
    }

    public List<UserDTO> getAllUsers(String search) {
        try {
            HttpRequest request = createGetAllUsersRequest(search);

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

    public List<FingerprintDTO> getUserFingerprints(Long userId) {
        try {
            HttpRequest request = createGetUserFingerprintsRequest(userId);

            HttpResponse<String> response = MyHttpClient.getInstance().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to retrieve user fingerprints. Status code: " + response.statusCode());
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
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public UserDTO getUserById(Long userId) {
        try {
            HttpRequest request = createGetUserByIdRequest(userId);

            HttpResponse<String> response = MyHttpClient.getInstance().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to retrieve user. Status code: " + response.statusCode());
            }

            return MyObjectMapper.getInstance().readValue(response.body(), UserDTO.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public List<FingerprintDTO> getFingerprintsByTypeAndUserRole(FingerType fingerType, Role role) {
        try {
            HttpRequest request = createGetFingerprintsByTypeAndUserRole(fingerType, role);

            HttpResponse<String> response = MyHttpClient.getInstance().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Failed to retrieve fingerprints. Status code: " + response.statusCode());
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

    private static HttpRequest createGetAllUsersRequest(String search) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/users?search=" + search))
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

    private static HttpRequest createGetUserFingerprintsRequest(Long userId) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/users/" + userId + "/fingerprints"))
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    private static HttpRequest createGetUserByIdRequest(Long userId) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/users/" + userId))
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    private static HttpRequest createGetFingerprintsByTypeAndUserRole(FingerType fingerType, Role role) {
        return HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/v1/fingerprints?fingerType=" + fingerType + "&role=" + role))
                .header("Accept", "application/json")
                .GET()
                .build();
    }
}
