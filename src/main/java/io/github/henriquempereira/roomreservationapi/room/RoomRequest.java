package io.github.henriquempereira.roomreservationapi.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record RoomRequest(
        @NotBlank String name,
        @Positive int capacity
){}
