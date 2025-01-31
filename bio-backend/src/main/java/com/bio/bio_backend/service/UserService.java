package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.*;
import com.bio.bio_backend.mapper.FingerprintMapper;
import com.bio.bio_backend.mapper.UserMapper;
import com.bio.bio_backend.model.*;
import com.bio.bio_backend.respository.FingerprintRepository;
import com.bio.bio_backend.respository.RoomRepository;
import com.bio.bio_backend.respository.UserRepository;
import com.bio.bio_backend.utils.EncryptionUtils;
import com.neurotec.biometrics.NSubject;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.bio.bio_backend.mapper.RoomMapper.toDTOS;
import static com.bio.bio_backend.mapper.UserMapper.toDTOS;

/**
 * Serwis odpowiedzialny za zarządzanie operacjami związanymi z użytkownikami.
 * Oferuje metody do tworzenia użytkowników, aktualizowania ich danych,
 * przypisywania do pokoi, zarządzania odciskami palców oraz wyszukiwania.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final FingerprintRepository fingerprintRepository;
    private final EnrollmentService enrollmentService;

    /**
     * Dodaje użytkownika wraz z jego odciskami palców i przypisuje go do pokoi.
     *
     * @param request żądanie zawierające dane użytkownika, odciski palców i identyfikatory pokoi.
     */
    @Transactional
    public void addUserWithFingerprintsAndRooms(UserCreationRequest request) {
        var user = new User(request.firstName(), request.lastName(), request.pesel(), request.role());

        for (Map.Entry<FingerType, byte[]> entry : request.fingerprintImageData().entrySet()) {
            try {
                // Odszyfruj dane przesłane w żądaniu
                byte[] decryptedImage = EncryptionUtils.decrypt(entry.getValue());

                NSubject subject = enrollmentService.createTemplateFromFile(decryptedImage).join();

                byte[] template = subject.getTemplateBuffer().toByteArray();
                try {
                    byte[] encryptedTemplate = EncryptionUtils.encrypt(template);

                    Fingerprint fingerprint = new Fingerprint(
                            encryptedTemplate,
                            entry.getKey(),
                            user,
                            entry.getValue()
                    );

                    user.getFingerprints().add(fingerprint);
                    fingerprintRepository.save(fingerprint);

                    System.out.println("Fingerprint added to user");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing fingerprint data", e);
            }
        }

        // Przypisanie użytkownikowi pomieszczeń (jeśli jakieś zostały przypisane)
        if (request.roomIds() != null) {
            request.roomIds().forEach(roomId -> {
                Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + roomId));
                user.addRoomToUser(room);
            });
        }

        userRepository.save(user);
    }

    /**
     * Pobiera listę wszystkich użytkowników.
     *
     * @return lista użytkowników w formacie DTO.
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return toDTOS(userRepository.findAll());
    }

    /**
     * Aktualizuje dane użytkownika na podstawie żądania.
     *
     * @param request żądanie zawierające nowe dane użytkownika.
     */
    @Transactional
    public void updateUser(UpdateUserRequest request) {
        var user = userRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.id()));

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPesel(request.pesel());
        user.setRole(Role.valueOf(request.role()));
    }

    /**
     * Usuwa użytkownika na podstawie ID.
     *
     * @param id ID użytkownika.
     * @return odpowiedź HTTP z kodem 204 (No Content).
     */
    @Transactional
    public ResponseEntity<Void> deleteUserWithId(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Pobiera listę pokoi przypisanych do użytkownika.
     *
     * @param userId ID użytkownika.
     * @return lista pokoi w formacie DTO.
     */
    @Transactional(readOnly = true)
    public List<RoomDTO> getUserRooms(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return toDTOS(user.getRooms());
    }

    /**
     * Usuwa przypisanie użytkownika do pokoju.
     *
     * @param userId ID użytkownika.
     * @param roomId ID pokoju.
     */
    @Transactional
    public void detachUserFromRoom(Long userId, Long roomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + roomId));

        user.removeRoomFromUser(room);
    }

    /**
     * Pobiera listę odcisków palców przypisanych do użytkownika.
     *
     * @param userId ID użytkownika.
     * @return lista odcisków palców w formacie DTO.
     */
    public List<FingerprintDTO> getUserFingerprints(Long userId) {
        List<Fingerprint> userFingerprints = fingerprintRepository.findByUserId(userId);


        return FingerprintMapper.toDTOS(userFingerprints);
    }

    /**
     * Pobiera dane użytkownika na podstawie ID.
     *
     * @param userId ID użytkownika.
     * @return obiekt użytkownika w formacie DTO.
     */
    public UserDTO getUserById(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        return UserMapper.toDTO(user);
    }

    /**
     * Wyszukuje użytkowników na podstawie podanego tekstu.
     *
     * @param search tekst do wyszukiwania (imię, nazwisko, PESEL itp.).
     * @return lista użytkowników spełniających kryteria wyszukiwania w formacie DTO.
     */
    public List<UserDTO> searchUsers(String search) {
        return toDTOS(userRepository.searchByFields(search));
    }
}
