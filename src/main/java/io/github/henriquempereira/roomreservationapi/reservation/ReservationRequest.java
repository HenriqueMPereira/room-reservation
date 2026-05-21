package io.github.henriquempereira.roomreservationapi.reservation;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationRequest(
        @NotNull LocalDateTime start,
        @NotNull LocalDateTime end,
        @NotNull Long userId,
        @NotNull Long roomId
){}
