package io.github.henriquempereira.roomreservationapi.room;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Representa uma sala física disponível para locação ou uso.
 * <p>
 *     Contém as especificações operacionais do espaço, incluindo
 *     limite de capacidade e disponibilidade atual.
 * </p>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @NotBlank(message = "A sala deve ter um nome.")
    @Column(name = "name", unique = true)
    private String roomName;

    @NotNull(message = "Capacidade é obrigatória.")
    @Positive(message = "Capacidade deve ser positiva.")
    private Integer capacity;

    @NotNull(message = "A sala deve ter status 'ATIVA' ou 'INATIVA'.")
    @Column(name = "status")
    private RoomStatus roomStatus;
}
