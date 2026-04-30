package io.github.henriquempereira.roomreservationapi.reservation;

import io.github.henriquempereira.roomreservationapi.room.Room;
import io.github.henriquempereira.roomreservationapi.user.User;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        LocalDateTime start,
        LocalDateTime end,
        User user,
        Room room,
        ReservationStatus reservationStatus
){}
