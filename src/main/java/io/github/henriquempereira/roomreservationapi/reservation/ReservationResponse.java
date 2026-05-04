package io.github.henriquempereira.roomreservationapi.reservation;

import io.github.henriquempereira.roomreservationapi.room.RoomSummaryResponse;
import io.github.henriquempereira.roomreservationapi.user.UserSummaryResponse;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        LocalDateTime start,
        LocalDateTime end,
        UserSummaryResponse user,
        RoomSummaryResponse room,
        ReservationStatus reservationStatus
){}
