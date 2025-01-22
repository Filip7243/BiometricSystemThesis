package com.bio.bio_backend.service;

import com.bio.bio_backend.dto.AddRoomRequest;
import com.bio.bio_backend.dto.AssignDeviceToRoomRequest;
import com.bio.bio_backend.dto.RoomDTO;
import com.bio.bio_backend.dto.UpdateRoomRequest;
import com.bio.bio_backend.mapper.RoomMapper;
import com.bio.bio_backend.model.Device;
import com.bio.bio_backend.model.Room;
import com.bio.bio_backend.respository.BuildingRepository;
import com.bio.bio_backend.respository.DeviceRepository;
import com.bio.bio_backend.respository.RoomRepository;
import com.bio.bio_backend.respository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.bio.bio_backend.mapper.RoomMapper.toDTO;

/**
 * Serwis odpowiedzialny za zarządzanie operacjami związanymi z pokojami.
 * Oferuje metody do dodawania, aktualizowania, usuwania i pobierania pokoi,
 * a także przypisywania urządzeń i użytkowników do pokoi.
 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final DeviceRepository deviceRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;

    /**
     * Aktualizuje dane istniejącego pokoju na podstawie jego ID.
     *
     * @param id      ID pokoju, który ma zostać zaktualizowany.
     * @param request obiekt żądania zawierający nowe dane pokoju.
     * @throws EntityNotFoundException jeśli pokój o podanym ID nie zostanie znaleziony.
     */
    @Transactional
    public void updateRoomWithId(Long id, UpdateRoomRequest request) {
        var room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room with id " + id + " not found"));

        room.setRoomNumber(request.roomNumber());
        room.setFloor(request.floor());
    }

    /**
     * Usuwa pokój na podstawie jego ID.
     *
     * @param id ID pokoju, który ma zostać usunięty.
     * @throws EntityNotFoundException jeśli pokój o podanym ID nie zostanie znaleziony.
     */
    @Transactional
    public void deleteRoomWithId(Long id) {
        var room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room with id " + id + " not found"));

        room.removeDevice();
        room.detachUsers();
        room.removeEnrollments();

        roomRepository.delete(room);
    }

    /**
     * Usuwa urządzenie przypisane do pokoju.
     *
     * @param request obiekt żądania zawierający ID pokoju i adres MAC urządzenia.
     * @throws EntityNotFoundException jeśli pokój lub urządzenie nie zostanie znalezione.
     */
    @Transactional
    public void removeDeviceFromRoom(AssignDeviceToRoomRequest request) {
        var room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new EntityNotFoundException("Room with id " + request.roomId() + " not found"));

        if (!deviceRepository.existsByMacAddress(request.macAddress())) {
            throw new EntityNotFoundException("Device with id " + request.macAddress() + " not found");
        }

        room.removeDevice();
    }

    /**
     * Przypisuje urządzenie do pokoju.
     *
     * @param request obiekt żądania zawierający ID pokoju i dane urządzenia.
     * @throws EntityNotFoundException jeśli pokój nie zostanie znaleziony.
     */
    @Transactional
    public void assignDeviceToRoom(AssignDeviceToRoomRequest request) {
        var room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new EntityNotFoundException("Room with id " + request.roomId() + " not found"));

        Device device;
        if (!deviceRepository.existsByMacAddress(request.macAddress())) {
            device = new Device(request.macAddress(), room, request.scannerSerialNumber());  // TODO: do zmiany
            deviceRepository.save(device);
        } else {
            device = deviceRepository.findByMacAddress(request.macAddress()).get();
        }

        room.setDevice(device);
    }

    /**
     * Dodaje nowy pokój do systemu.
     *
     * @param request obiekt żądania zawierający dane nowego pokoju.
     * @return obiekt DTO zawierający dane dodanego pokoju.
     * @throws EntityNotFoundException jeśli budynek o podanym ID nie zostanie znaleziony.
     * @throws IllegalArgumentException jeśli urządzenie jest już przypisane do innego pokoju.
     */
    @Transactional
    public RoomDTO addRoom(AddRoomRequest request) {
        var building = buildingRepository.findById(request.buildingId())
                .orElseThrow(() -> new EntityNotFoundException("Building with id " + request.buildingId() + " not found"));

        Device device;
        // Jeśli urządzenie nie istnieje to dodaj do bazy danych
        if (!deviceRepository.existsByMacAddress(request.macAddress())) {
            device = new Device(request.macAddress(), null, request.scannerSerialNumber());  // TODO: do zminay
            deviceRepository.save(device);
        } else {
            device = deviceRepository.findByMacAddress(request.macAddress())
                    .get();

            // Jeśli urządzenie jest przypisane do jakiegoś pokoju wyrzuć wyjątek,
            // urządzenie nie może być przypisane do więcej niż jednego pomieszczenia
            if (device.getRoom() != null) {
                throw new IllegalArgumentException("Device with mac address " + request.macAddress() + " is already assigned to room");
            }
        }

        var room = new Room(request.roomNumber(), request.floor(), building, device);

        roomRepository.save(room);

        return toDTO(room);
    }

    /**
     * Przypisuje pokój do użytkownika.
     *
     * @param roomId ID pokoju.
     * @param userId ID użytkownika.
     * @throws EntityNotFoundException jeśli pokój lub użytkownik nie zostaną znalezieni.
     */
    @Transactional
    public void assignRoomToUser(Long roomId, Long userId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room with id " + roomId + " not found"));
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + userId + " not found"));

        user.addRoomToUser(room);
    }

    /**
     * Pobiera dane pokoju na podstawie jego ID.
     *
     * @param roomId ID pokoju.
     * @return obiekt DTO zawierający dane pokoju.
     * @throws EntityNotFoundException jeśli pokój o podanym ID nie zostanie znaleziony.
     */
    @Transactional(readOnly = true)
    public RoomDTO getRoomById(Long roomId) {
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room with id " + roomId + " not found"));

        return toDTO(room);
    }

    /**
     * Pobiera listę wszystkich pokoi w systemie.
     *
     * @return lista obiektów DTO zawierających dane pokoi.
     */
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(RoomMapper::toDTO)
                .collect(Collectors.toList());
    }
}
