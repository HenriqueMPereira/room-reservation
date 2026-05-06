package io.github.henriquempereira.roomreservationapi.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Reservation r " +
            "WHERE r.room.id = :roomId " +
            "AND r.reservationStatus = :status " +
            "AND r.start < :endTime "+
            "AND r.end > :startTime")
    boolean hasOverlap(@Param("roomId") Long roomId,
                       @Param("startTime") LocalDateTime startTime,
                       @Param("endTime") LocalDateTime endTime,
                       @Param("status") ReservationStatus status);


    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Reservation r " +
            "WHERE r.room.id = :roomId " +
            "AND r.reservationStatus = :status " +
            "AND r.id != :reservationId " +
            "AND r.start < :endTime " +
            "AND r.end > :startTime")
    boolean hasOverlapIgnoringId(@Param("roomId") Long roomId,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime,
                                 @Param("reservationId") Long reservationId,
                                 @Param("status") ReservationStatus status);

}
