package io.github.henriquempereira.roomreservationapi.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository repository;

    public RoomResponse createRoom(RoomRequest request) {
        if(repository.existsByRoomName(request.name())){
            throw new IllegalArgumentException("Já existe uma sala cadastrada com este nome");
        }
        Room room = new Room();
        room.setRoomName(request.name());
        room.setCapacity(request.capacity());

        Room savedRoom = repository.save(room);

        return toResponse(savedRoom);
    }

    public List<RoomResponse> getAllRooms() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public RoomResponse getRoomById(Long id) {
        Room room = getRoomOrThrow(id);
        return toResponse(room);
    }

    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = getRoomOrThrow(id);

        if (!room.getRoomName().equals(request.name()) && repository.existsByRoomName(request.name())) {
            throw new IllegalArgumentException("Já existe uma sala cadastrada com este nome.");
        }

        room.setRoomName(request.name());
        room.setCapacity(request.capacity());

        Room savedRoom = repository.save(room);
        return toResponse(savedRoom);
    }

    public void deleteRoomById(Long id) {
        Room room = getRoomOrThrow(id);
        room.setRoomStatus(RoomStatus.INATIVA);
        repository.save(room);
    }

    private Room getRoomOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada com o ID: " + id));
    }

    private RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getRoomName(),
                room.getCapacity(),
                room.getRoomStatus()
        );
    }

}
