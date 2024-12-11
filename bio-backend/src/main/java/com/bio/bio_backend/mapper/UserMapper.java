package com.bio.bio_backend.mapper;

import com.bio.bio_backend.dto.FingerprintDTO;
import com.bio.bio_backend.dto.RoomDTO;
import com.bio.bio_backend.dto.UserDTO;
import com.bio.bio_backend.model.User;

import java.util.List;

public final class UserMapper {

    private UserMapper() {

    }

    public static UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPesel(),
                user.getRole().name(),
                RoomMapper.toDTOS(user.getRooms()),
                FingerprintMapper.toDTOS(user.getFingerprints())
        );
    }

    public static List<UserDTO> toDTOS(List<User> users) {
        return users.stream()
                .map(UserMapper::toDTO)
                .toList();
    }
}
