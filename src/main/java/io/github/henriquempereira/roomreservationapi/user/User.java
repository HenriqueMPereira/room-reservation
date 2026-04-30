package io.github.henriquempereira.roomreservationapi.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.br.CPF;

/**
 * Representa um usuário do sistema de reservas.
 * <p>
 *     Entidade responsável por armazenar os dados de identificação
 *     dos clientes que podem solicitar agendamentos de salas.
 * </p>
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotBlank(message = "Usuário deve ter um nome.")
    @Column(name = "name")
    private String userName;

    @CPF(message = "Usuário deve ter um CPF válido.")
    @NotBlank(message = "Usuário deve ter um CPF.")
    private String cpf;
}
