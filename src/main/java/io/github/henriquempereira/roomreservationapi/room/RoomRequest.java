package io.github.henriquempereira.roomreservationapi.room;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RoomRequest(
        @NotBlank(message = "O nome da sala é obrigatório")
        String name,

        @NotNull(message = "A capacidade é obrigatória")
        @Positive(message = "A capacidade deve ser maior que zero")
        Integer capacity,

        @NotNull(message = "O status da sala é obrigatório")
        RoomStatus status
){}
