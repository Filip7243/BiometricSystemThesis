package com.bio.bio_backend.mapper;

import com.bio.bio_backend.dto.UserDTO;
import com.bio.bio_backend.model.User;

import java.util.List;

/**
 * Klasa do mapowania obiektów typu {@link User} na ich odpowiedniki DTO ({@link UserDTO}).
 * Zawiera metody statyczne, które przekształcają obiekty typu {@link User} oraz kolekcje tych obiektów na obiekty DTO.
 */
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
