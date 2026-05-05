package io.github.henriquempereira.roomreservationapi.room;

import org.springframework.data.jpa.repository.JpaRepository;


public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByRoomName(String roomName);
}
