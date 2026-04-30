package io.github.henriquempereira.roomreservationapi.room;

public record RoomResponse(
        Long id,
        String name,
        Integer capacity,
        RoomStatus status
){}
