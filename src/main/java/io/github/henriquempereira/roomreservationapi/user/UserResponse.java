package io.github.henriquempereira.roomreservationapi.user;

public record UserResponse(
        Long id,
        String name,
        String cpf
){}
