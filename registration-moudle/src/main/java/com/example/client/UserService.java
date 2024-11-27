package com.example.client;

import com.example.client.dto.UpdateUserRequest;
import com.example.client.dto.UserDTO;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class UserService {

    private final UserClient userClient;

    public UserService(UserClient userClient) {
        this.userClient = userClient;
    }

    public void updateUser(UpdateUserRequest request, Consumer<Void> onSuccess, Component parentComponent) {
        BaseResourceWorker.execute(
                () -> {
                    userClient.updateUser(request);
                    return null;
                },
                onSuccess,
                parentComponent
        );
    }

    public void getAllUsers(Consumer<List<UserDTO>> onSuccess, Component parentComponent) {
        System.out.println("GETTING users!");
        BaseResourceWorker.execute(
                userClient::getAllUsers,
                onSuccess,
                parentComponent
        );
    }

    public void deleteUser(Long userId, Consumer<Void> onSuccess, Component parentComponent) {
        BaseResourceWorker.execute(
                () -> {
                    userClient.deleteUser(userId);
                    return null;
                },
                onSuccess,
                parentComponent
        );
    }
}
