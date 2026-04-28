package io.github.henriquempereira.roomreservationapi.room;

/**
 * Define os possíveis estados operacionais de uma sala.
 */
public enum RoomStatus {
    /** A sala está disponível para uso e novas reservas. */
    ATIVA,

    /** A sala está indisponível (ex: em manutenção ou desativada). */
    INATIVA
}