package io.github.henriquempereira.roomreservationapi.reservation;

import io.github.henriquempereira.roomreservationapi.room.Room;
import io.github.henriquempereira.roomreservationapi.room.RoomRepository;
import io.github.henriquempereira.roomreservationapi.user.User;
import io.github.henriquempereira.roomreservationapi.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Nested
    @DisplayName("Método: createReservation")
    class CreateReservation {
        @Test
        @DisplayName("Deve criar reserva com data válida, usuário e sala existentes e sem sobreposição de horário")
        void shouldCreateReservation() {
            ReservationRequest reservationRequest = createValidRequest();
            Room roomFromDb = createMockRoom();
            User userFromDb = createMockUser();

            Reservation savedReservation = new Reservation();
            savedReservation.setId(1L);
            savedReservation.setStart(reservationRequest.start());
            savedReservation.setEnd(reservationRequest.end());
            savedReservation.setRoom(roomFromDb);
            savedReservation.setUser(userFromDb);

            when(roomRepository.findById(roomFromDb.getId())).thenReturn(Optional.of(roomFromDb));
            when(userRepository.findById(userFromDb.getId())).thenReturn(Optional.of(userFromDb));
            when(reservationRepository.hasOverlap(
                    roomFromDb.getId(),
                    reservationRequest.start(),
                    reservationRequest.end(),
                    ReservationStatus.ATIVA)).thenReturn(false);
            when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

            // ACT
            ReservationResponse response = reservationService.createReservation(reservationRequest);

            // ASSERT
            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals("Henrique", response.user().userName());
            assertEquals("4-001", response.room().roomName());
            assertEquals(reservationRequest.start(), response.start());
            assertEquals(reservationRequest.end(), response.end());

            verify(roomRepository, times(1)).findById(roomFromDb.getId());
            verify(userRepository, times(1)).findById(userFromDb.getId());
            verify(reservationRepository, times(1)).hasOverlap(
                    roomFromDb.getId(),
                    reservationRequest.start(),
                    reservationRequest.end(),
                    ReservationStatus.ATIVA);
            verify(reservationRepository, times(1)).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar criar reserva quando o horário de fim é maior ou igual ao início")
        void shouldThrowExceptionWhenDatesAreInvalid() {
            LocalDateTime start = LocalDateTime.of(2026, 5, 19, 12, 0);
            LocalDateTime end = LocalDateTime.of(2026, 5, 19, 10, 0);
            Long roomId = 1L;
            Long userId = 1L;

            ReservationRequest request = new ReservationRequest(
                    start,
                    end,
                    userId,
                    roomId
            );

            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.createReservation(request));

            assertEquals("A data/hora de início deve ser anterior à data/hora de término.", exception.getMessage());
            verify(roomRepository, never()).findById(anyLong());
            verify(userRepository, never()).findById(anyLong());
            verify(reservationRepository, never()).hasOverlap(request.roomId(), start, end, ReservationStatus.ATIVA);
            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar criar reserva quando a sala estiver ocupada no horário")
        void shouldThrowExceptionWhenHasOverlap() {
            ReservationRequest request = createValidRequest();
            Room roomFromDB = createMockRoom();
            User userFromDB = createMockUser();

            when(roomRepository.findById(roomFromDB.getId())).thenReturn(Optional.of(roomFromDB));
            when(userRepository.findById(userFromDB.getId())).thenReturn(Optional.of(userFromDB));
            when(reservationRepository.hasOverlap(
                    roomFromDB.getId(),
                    request.start(),
                    request.end(),
                    ReservationStatus.ATIVA)).thenReturn(true);

            // ACT & ASSERT
            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.createReservation(request));

            assertEquals("A sala já possui reserva neste horário.", exception.getMessage());
            verify(roomRepository, times(1)).findById(roomFromDB.getId());
            verify(userRepository, times(1)).findById(userFromDB.getId());
            verify(reservationRepository, times(1)).hasOverlap(
                    roomFromDB.getId(),
                    request.start(),
                    request.end(),
                    ReservationStatus.ATIVA);
            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando a sala a ser reservada não existir")
        void shouldThrowExceptionWhenRoomNotFound() {
            LocalDateTime start = LocalDateTime.of(2026, 5, 19, 12, 0);
            LocalDateTime end = LocalDateTime.of(2026, 5, 19, 14, 0);
            Long roomId = 99L;
            Long userId = 1L;

            ReservationRequest request = new ReservationRequest(
                    start,
                    end,
                    userId,
                    roomId
            );

            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.createReservation(request));

            assertEquals("Sala não encontrada!", exception.getMessage());
            verify(roomRepository, times(1)).findById(roomId);
            verify(userRepository, never()).findById(anyLong());
            verify(reservationRepository, never()).hasOverlap(roomId, start, end, ReservationStatus.ATIVA);
            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando o usuário não existir")
        void shouldThrowExceptionWhenUserNotFound() {
            LocalDateTime start = LocalDateTime.of(2026, 5, 19, 12, 0);
            LocalDateTime end = LocalDateTime.of(2026, 5, 19, 14, 0);
            Long roomId = 1L;
            Long userId = 99L;

            ReservationRequest request = new ReservationRequest(
                    start,
                    end,
                    userId,
                    roomId
            );

            Room roomFromDB = new Room();
            roomFromDB.setId(roomId);
            roomFromDB.setRoomName("4-001");

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomFromDB));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.createReservation(request));

            assertEquals("Usuário não encontrado!", exception.getMessage());
            verify(roomRepository, times(1)).findById(roomId);
            verify(userRepository, times(1)).findById(userId);
            verify(reservationRepository, never()).hasOverlap(
                    anyLong(),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class),
                    any(ReservationStatus.class));
            verify(reservationRepository, never()).save(any(Reservation.class));
        }
    }

    @Nested
    @DisplayName("Método: uptadeReservation")
    class UpdateReservation {
        @Test
        @DisplayName("Deve atualizar uma reserva quando horário for válido, sala e usuário existirem e não ter conflito de data")
        void shouldUpdateReservation() {
            ReservationRequest request = createValidRequest();
            Room roomFromDb = createMockRoom();
            User userFromDb = createMockUser();
            Long id = 1L;

            Reservation reservationFromDb = createReservationFromDb(id, roomFromDb, userFromDb);

            Reservation savedReservation = new Reservation();
            savedReservation.setId(id);
            savedReservation.setStart(request.start());
            savedReservation.setEnd(request.end());
            savedReservation.setRoom(roomFromDb);
            savedReservation.setUser(userFromDb);

            when(roomRepository.findById(roomFromDb.getId())).thenReturn(Optional.of(roomFromDb));
            when(userRepository.findById(userFromDb.getId())).thenReturn(Optional.of(userFromDb));
            when(reservationRepository.hasOverlapIgnoringId(
                    roomFromDb.getId(),
                    request.start(),
                    request.end(),
                    id,
                    ReservationStatus.ATIVA
            )).thenReturn(false);
            when(reservationRepository.findById(id)).thenReturn(Optional.of(reservationFromDb));
            when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

            ReservationResponse response = reservationService.updateReservation(id, request);

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertEquals("Henrique", response.user().userName());
            assertEquals("4-001", response.room().roomName());
            assertEquals(request.start(), response.start());
            assertEquals(request.end(), response.end());

            verify(roomRepository, times(1)).findById(request.roomId());
            verify(userRepository, times(1)).findById(request.userId());
            verify(reservationRepository, times(1)).hasOverlapIgnoringId(
                    roomFromDb.getId(),
                    request.start(),
                    request.end(),
                    id,
                    ReservationStatus.ATIVA
            );
            verify(reservationRepository, times(1)).findById(id);
            verify(reservationRepository, times(1)).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar atualizar reserva quando o horário do fim é maior ou igual ao início")
        void shouldThrowExceptionWhenDatesAreInvalid() {
            LocalDateTime start = LocalDateTime.of(2026, 5, 19, 20, 0);
            LocalDateTime end = LocalDateTime.of(2026, 5, 19, 20, 0);
            Long validId = 1L;
            ReservationRequest request = new ReservationRequest(
                    start,
                    end,
                    validId,
                    validId
            );
            Long id = 1L;

            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.updateReservation(id, request));

            assertEquals("A data/hora de início deve ser anterior à data/hora de término.", exception.getMessage());
            verify(roomRepository, never()).findById(anyLong());
            verify(userRepository, never()).findById(anyLong());
            verify(reservationRepository, never()).hasOverlapIgnoringId(
                    anyLong(),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class),
                    anyLong(),
                    any(ReservationStatus.class)
            );
            verify(reservationRepository, never()).findById(anyLong());
            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar atualizar reserva quando a sala não existir")
        void shouldThrowExceptionWhenRoomNotFound() {
            LocalDateTime start = LocalDateTime.of(2026, 5, 19, 18, 0);
            LocalDateTime end = LocalDateTime.of(2026, 5, 19, 20, 0);
            Long userId = 1L;
            Long roomNotFoundId = 99L;
            ReservationRequest request = new ReservationRequest(
                    start,
                    end,
                    userId,
                    roomNotFoundId
            );
            Long id = 1L;

            when(roomRepository.findById(request.roomId())).thenReturn(Optional.empty());

            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.updateReservation(id, request));

            assertEquals("Sala não encontrada!", exception.getMessage());

            verify(roomRepository, times(1)).findById(request.roomId());
            verify(userRepository, never()).findById(anyLong());
            verify(reservationRepository, never()).hasOverlapIgnoringId(
                    anyLong(),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class),
                    anyLong(),
                    any(ReservationStatus.class)
            );
            verify(reservationRepository, never()).findById(anyLong());
            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar atualizar reserva quando o usuário não existir")
        void shouldThrowExceptionWhenUserNotFound() {
            LocalDateTime start = LocalDateTime.of(2026, 5, 19, 18, 0);
            LocalDateTime end = LocalDateTime.of(2026, 5, 19, 20, 0);
            Long userNotFound = 99L;
            Long roomId = 1L;
            ReservationRequest request = new ReservationRequest(
                    start,
                    end,
                    userNotFound,
                    roomId
            );
            Long id = 1L;
            Room roomFromDb = createMockRoom();

            when(roomRepository.findById(request.roomId())).thenReturn(Optional.of(roomFromDb));
            when(userRepository.findById(request.userId())).thenReturn(Optional.empty());
            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.updateReservation(id, request));

            assertEquals("Usuário não encontrado!", exception.getMessage());

            verify(roomRepository, times(1)).findById(request.roomId());
            verify(userRepository, times(1)).findById(request.userId());
            verify(reservationRepository, never()).hasOverlapIgnoringId(
                    anyLong(),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class),
                    anyLong(),
                    any(ReservationStatus.class)
            );
            verify(reservationRepository, never()).findById(anyLong());
            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar atualizar reserva quando a sala estiver ocupada no horários")
        void shouldThrowExceptionWhenHasOverlap() {
            ReservationRequest request = createValidRequest();
            Room roomFromDb = createMockRoom();
            User userFromDb = createMockUser();
            Long id = 1L;

            when(roomRepository.findById(request.roomId())).thenReturn(Optional.of(roomFromDb));
            when(userRepository.findById(request.userId())).thenReturn(Optional.of(userFromDb));
            when(reservationRepository.hasOverlapIgnoringId(
                    request.roomId(),
                    request.start(),
                    request.end(),
                    id,
                    ReservationStatus.ATIVA
                    )).thenReturn(true);

            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.updateReservation(id, request));

            assertEquals("A sala já possui reserva neste horário.", exception.getMessage());

            verify(roomRepository, times(1)).findById(request.roomId());
            verify(userRepository, times(1)).findById(request.userId());
            verify(reservationRepository, times(1)).hasOverlapIgnoringId(
                    request.roomId(),
                    request.start(),
                    request.end(),
                    id,
                    ReservationStatus.ATIVA
            );
            verify(reservationRepository, never()).findById(anyLong());
            verify(reservationRepository, never()).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar atualizar reserva e não for encontrada a reserva a ser atualizada")
        void shouldThrowExceptionWhenReservationNotFound() {
            ReservationRequest request = createValidRequest();
            Room roomFromDb = createMockRoom();
            User userFromDb = createMockUser();
            Long id = 99L;

            when(roomRepository.findById(request.roomId())).thenReturn(Optional.of(roomFromDb));
            when(userRepository.findById(request.userId())).thenReturn(Optional.of(userFromDb));
            when(reservationRepository.hasOverlapIgnoringId(
                    request.roomId(),
                    request.start(),
                    request.end(),
                    id,
                    ReservationStatus.ATIVA
            )).thenReturn(false);
            when(reservationRepository.findById(id)).thenReturn(Optional.empty());

            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.updateReservation(id, request));

            assertEquals(("Não existe reserva cadastrada neste ID: %d".formatted(id)), exception.getMessage());

            verify(roomRepository, times(1)).findById(request.roomId());
            verify(userRepository, times(1)).findById(request.userId());
            verify(reservationRepository, times(1)).hasOverlapIgnoringId(
                    request.roomId(),
                    request.start(),
                    request.end(),
                    id,
                    ReservationStatus.ATIVA
            );
            verify(reservationRepository, times(1)).findById(id);
            verify(reservationRepository, never()).save(any(Reservation.class));
        }
    }

    @Nested
    @DisplayName("Método: cancelReservation")
    class CancelReservation {

        @Test
        @DisplayName("Deve cancelar uma reserva com sucesso quando o ID existir")
        void shouldCancelReservation() {
            Long id = 1L;
            Reservation reservationFromDb = new Reservation();
            reservationFromDb.setId(id);

            when(reservationRepository.findById(id)).thenReturn(Optional.of(reservationFromDb));

            reservationService.cancelReservation(id);

            assertEquals(ReservationStatus.CANCELADA, reservationFromDb.getReservationStatus());

            verify(reservationRepository, times(1)).findById(id);
            verify(reservationRepository, times(1)).save(any(Reservation.class));
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar cancelar uma reserva quando o ID não existir")
        void shouldThrowExceptionWhenReservationNotFound() {
            Long id = 99L;

            when(reservationRepository.findById(id)).thenReturn(Optional.empty());

            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    reservationService.cancelReservation(id));

            assertEquals(("Não existe reserva cadastrada neste ID: %d".formatted(id)), exception.getMessage());

            verify(reservationRepository, times(1)).findById(id);
            verify(reservationRepository, never()).save(any(Reservation.class));
        }
    }

    private Reservation createReservationFromDb(Long id, Room roomFromDb, User userFromDb) {
        LocalDateTime start = LocalDateTime.of(2026, 5, 19, 20, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 19, 22, 0);
        Reservation reservationFromDb = new Reservation();
        reservationFromDb.setId(id);
        reservationFromDb.setStart(start);
        reservationFromDb.setEnd(end);
        reservationFromDb.setRoom(roomFromDb);
        reservationFromDb.setUser(userFromDb);
        return reservationFromDb;
    }

    private ReservationRequest createValidRequest() {
        return new ReservationRequest(
                LocalDateTime.of(2026, 5, 20, 10, 0),
                LocalDateTime.of(2026, 5, 20, 12, 0),
                1L,
                1L
        );
    }

    private Room createMockRoom() {
        Room room = new Room();
        room.setId(1L);
        room.setRoomName("4-001");
        return room;
    }

    private User createMockUser() {
        User user = new User();
        user.setId(1L);
        user.setUserName("Henrique");
        return user;
    }
}