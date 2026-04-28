package io.github.henriquempereira.roomreservationapi.reservation;

/**
 * Define o ciclo de vida e os estados de uma reserva.
 */
public enum ReservationStatus {
    /** Reserva confirmada e aguardando utilização. */
    ATIVA,

    /** Reserva cancelada pelo usuário ou pelo sistema. */
    CANCELADA,

    /** Reserva utilizada e finalizada com sucesso. */
    CONCLUÍDA
}