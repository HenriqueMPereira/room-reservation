package io.github.henriquempereira.roomreservationapi.reservation;

import io.github.henriquempereira.roomreservationapi.room.Room;
import io.github.henriquempereira.roomreservationapi.user.User;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationRequest(
        @NotNull LocalDateTime start,
        @NotNull LocalDateTime end,
        @NotNull User user,
        @NotNull Room room
){}
