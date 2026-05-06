package io.github.henriquempereira.roomreservationapi.room;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository repository;

    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        if(repository.existsByRoomName(request.name())){
            throw new IllegalArgumentException("Já existe uma sala cadastrada com este nome");
        }
        Room room = new Room();
        room.setRoomName(request.name());
        room.setCapacity(request.capacity());
        room.setRoomStatus(request.status());
        Room savedRoom = repository.save(room);

        return toResponse(savedRoom);
    }

    public Page<RoomResponse> getAllRooms(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public RoomResponse getRoomById(Long id) {
        Room room = getRoomOrThrow(id);
        return toResponse(room);
    }

    @Transactional
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

    @Transactional
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
