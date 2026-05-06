package io.github.henriquempereira.roomreservationapi.reservation;

import io.github.henriquempereira.roomreservationapi.room.Room;
import io.github.henriquempereira.roomreservationapi.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Representa a reserva de uma sala feita por um usuário.
 * <p>
 *     Entidade central que encapsula a lógica do ciclo de vida de uma reserva de sala,
 *     garantindo transições de estado da reserva.
 * </p>
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull(message = "A reserva deve ter um horário de início.")
    @Column(name = "start_time")
    private LocalDateTime start;

    @NotNull(message = "A reserva deve ter um horário de fim.")
    @Column(name = "end_time")
    private LocalDateTime end;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "A reserva deve ter uma sala.")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message = "A reserva deve ter um usuário.")
    private User user;

    @Setter(AccessLevel.NONE)
    private ReservationStatus reservationStatus = ReservationStatus.ATIVA;

    /**
     * Valida a coerência do período da reserva.
     * * @return {@code true} se as datas forem nulas (delegando a validação para o @NotNull)
     * ou se a data de início for anterior à data de fim. {@code false} caso contrário.
     */
    @AssertTrue(message = "A data de início deve ser rigorosamente anterior à data de fim.")
    public boolean isValidPeriod() {
        if(this.start == null || this.end == null) {
            return true; // para deixar o @NotNull fazer o trabalho dele
        }
        return this.start.isBefore(this.end);
    }

    /**
     * Realiza o cancelamento da reserva.
     *
     * @throws IllegalStateException se a reserva não estiver no estado {@link ReservationStatus#ATIVA}.
     */
    public void cancelReservation() {
        if(this.reservationStatus != ReservationStatus.ATIVA) {
            throw new IllegalStateException("Apenas reservas ativas podem ser canceladas.");
        } else {
            this.reservationStatus = ReservationStatus.CANCELADA;
        }
    }

    /**
     * Conclui a reserva indicando que o uso da sala foi finalizado com sucesso.
     *
     * @throws IllegalStateException se a reserva não estiver no estado {@link ReservationStatus#ATIVA}.
     */
    public void completeReservation() {
        if(this.reservationStatus != ReservationStatus.ATIVA){
            throw new IllegalStateException("Apenas reservas ativas podem ser concluídas.");
        }
    else {
        this.reservationStatus = ReservationStatus.CONCLUÍDA;
        }
    }
}
