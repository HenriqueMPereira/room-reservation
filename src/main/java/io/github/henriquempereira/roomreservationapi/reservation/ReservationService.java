package io.github.henriquempereira.roomreservationapi.reservation;

import io.github.henriquempereira.roomreservationapi.room.Room;
import io.github.henriquempereira.roomreservationapi.room.RoomRepository;
import io.github.henriquempereira.roomreservationapi.room.RoomSummaryResponse;
import io.github.henriquempereira.roomreservationapi.user.User;
import io.github.henriquempereira.roomreservationapi.user.UserRepository;
import io.github.henriquempereira.roomreservationapi.user.UserSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;


    public ReservationResponse createReservation(ReservationRequest request) {
        validateDates(request.start(), request.end());

        Room room = roomRepository.findById(request.room().getId())
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada!"));
        User user = userRepository.findById(request.user().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado!"));

        if(reservationRepository.hasOverlap(request.room().getId(), request.start(), request.end())) {
            throw new IllegalArgumentException("A sala já possui reserva neste horário.");
        }

        Reservation reservation = new Reservation();
        reservation.setStart(request.start());
        reservation.setEnd(request.end());
        reservation.setUser(user);
        reservation.setRoom(room);

        Reservation savedReservation = reservationRepository.save(reservation);
        return toResponse(savedReservation);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = getReservationOrThrow(id);
        return toResponse(reservation);
    }

    public ReservationResponse updateReservation(Long id, ReservationRequest request) {
        validateDates(request.start(), request.end());
        Room room = roomRepository.findById(request.room().getId())
                .orElseThrow(() -> new IllegalArgumentException("Sala não encontrada!"));
        User user = userRepository.findById(request.user().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado!"));

        if(reservationRepository.hasOverlapIgnoringId(room.getId(), request.start(), request.end(), id)){
            throw new IllegalArgumentException("A sala já possui reserva neste horário.");
        }
        Reservation reservation = getReservationOrThrow(id);
        reservation.setRoom(room);
        reservation.setUser(user);
        reservation.setStart(request.start());
        reservation.setEnd(request.end());

        Reservation savedReservation = reservationRepository.save(reservation);
        return toResponse(savedReservation);
    }

    public void cancelReservation(Long id) {
        Reservation reservation = getReservationOrThrow(id);
        reservation.cancelReservation();
        reservationRepository.save(reservation);
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new IllegalArgumentException("A data/hora de início deve ser anterior à data/hora de término.");
        }
    }

    private Reservation getReservationOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Não existe reserva cadastrada neste ID: " + id));
    }

    private ReservationResponse toResponse(Reservation reservation) {

        UserSummaryResponse userSummaryResponse = new UserSummaryResponse(
                reservation.getUser().getId(),
                reservation.getUser().getUserName()
        );

        RoomSummaryResponse roomSummaryResponse = new RoomSummaryResponse(
                reservation.getRoom().getId(),
                reservation.getRoom().getRoomName()
        );

        return new ReservationResponse(
                reservation.getId(),
                reservation.getStart(),
                reservation.getEnd(),
                userSummaryResponse,
                roomSummaryResponse,
                reservation.getReservationStatus()
        );
    }
}
