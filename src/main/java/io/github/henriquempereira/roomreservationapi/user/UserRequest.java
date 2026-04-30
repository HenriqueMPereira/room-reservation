package io.github.henriquempereira.roomreservationapi.user;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record UserRequest(
        @NotBlank String name,
        @CPF String cpf
){}
