package io.github.henriquempereira.roomreservationapi.room;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Suíte de testes unitários para a classe {@link RoomService}.
 * <p>
 * Valida as regras de negócio associadas à criação, atualização e desativação de salas,
 * garantindo o isolamento da camada de persistência através da injeção de mocks.
 * </p>
 * <p>
 * Principais cenários cobertos:
 * <ul>
 * <li>Criação de salas com validação de unicidade do nome (prevenção de duplicidade).</li>
 * <li>Atualização de dados da sala (nome e capacidade), garantindo que nomes atualizados não gerem conflito.</li>
 * <li>Desativação lógica (soft delete) da sala, bloqueando tentativas de desativar entidades já inativas.</li>
 * <li>Tratamento consistente de exceções (entidade não encontrada ou estados inválidos).</li>
 * </ul>
 * </p>
 */
@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    @DisplayName("Deve criar uma sala quando o nome não existir no banco")
    void shouldCreateRoomSuccessfully() {
        // ARRANGE
        RoomRequest request = new RoomRequest(
                "4-001",
                50,
                RoomStatus.ATIVA
        );

        Room roomMockFromDataBase = new Room();
        roomMockFromDataBase.setId(1L);
        roomMockFromDataBase.setRoomName("4-001");
        roomMockFromDataBase.setCapacity(50);
        roomMockFromDataBase.setRoomStatus(RoomStatus.ATIVA);

        when(roomRepository.existsByRoomName(request.name())).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(roomMockFromDataBase);

        // ACT
        RoomResponse response = roomService.createRoom(request);

        // ASSERT
        assertNotNull(response);
        assertEquals("4-001", response.name());
        assertEquals(50, response.capacity());
        assertEquals(RoomStatus.ATIVA, response.status());

        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar sala com nome que já existe")
    void shouldThrowExceptionWhenNameAlreadyExists() {
        // ARRANGE
        RoomRequest request = new RoomRequest(
                "4-001",
                50,
                RoomStatus.ATIVA
        );
        when(roomRepository.existsByRoomName(request.name())).thenReturn(true);

        // ACT & ASSERT
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
           roomService.createRoom(request);
        });

        assertEquals("Já existe uma sala cadastrada com este nome", exception.getMessage());
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    @DisplayName("Deve atualizar uma sala com sucesso se o ID existir e o novo nome já não estiver cadastrado")
    void shouldUpdateRoomSuccessfully() {
        // ARRANGE
        Long id = 1L;
        RoomRequest request = new RoomRequest(
                "4-001",
                50,
                RoomStatus.ATIVA
        );

        Room roomFromDataBase = new Room();
        roomFromDataBase.setId(id);
        roomFromDataBase.setRoomName("Nome Antigo");
        roomFromDataBase.setCapacity(30);
        roomFromDataBase.setRoomStatus(RoomStatus.ATIVA);

        Room updatedRoom = new Room();
        updatedRoom.setId(id);
        updatedRoom.setRoomName(request.name());
        updatedRoom.setCapacity(request.capacity());
        updatedRoom.setRoomStatus(request.status());

        when(roomRepository.findById(id)).thenReturn(Optional.of(roomFromDataBase));
        when(roomRepository.existsByRoomName(request.name())).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(updatedRoom);

        // ACT
        RoomResponse response = roomService.updateRoom(id, request);

        // ASSERT
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("4-001", response.name());
        assertEquals(50, response.capacity());
        assertEquals(RoomStatus.ATIVA, response.status());

        verify(roomRepository, times(1)).findById(id);
        verify(roomRepository, times(1)).existsByRoomName(request.name());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("Deve atualizar apenas a capacidade da sala")
    void shouldUpdateJustCapacity() {
        // ARRANGE
        Long id = 1L;
        RoomRequest request = new RoomRequest(
                "4-001",
                30,
                RoomStatus.ATIVA
        );

        Room roomFromDataBase = new Room();
        roomFromDataBase.setId(id);
        roomFromDataBase.setRoomName("4-001");
        roomFromDataBase.setCapacity(50);
        roomFromDataBase.setRoomStatus(RoomStatus.ATIVA);

        when(roomRepository.findById(id)).thenReturn(Optional.of(roomFromDataBase));
        when(roomRepository.save(any(Room.class))).thenReturn(roomFromDataBase);

        // ACT
        RoomResponse response = roomService.updateRoom(id, request);

        // ASSERT
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("4-001", response.name());
        assertEquals(30, response.capacity());
        assertEquals(RoomStatus.ATIVA, response.status());

        verify(roomRepository, times(1)).findById(id);
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar sala com id inexistente")
    void shouldThrowExceptionWhenIdNotFound() {
        // ARRANGE
        Long idNotFound = 99L;
        RoomRequest request = new RoomRequest(
                "4-001",
                50,
                RoomStatus.ATIVA
        );

        when(roomRepository.findById(idNotFound)).thenReturn(Optional.empty());

        // ACT & ASSERT
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                roomService.updateRoom(idNotFound, request));
        assertEquals("Sala não encontrada com o ID: 99", exception.getMessage());
        verify(roomRepository, never()).existsByRoomName(request.name());
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar sala mas outra sala já tem o nome a ser atualizado")
    void shouldThrowExceptionWhenAnotherRoomHasSameName() {
        // ARRANGE
        Long id = 1L;
        RoomRequest request = new RoomRequest(
                "Nome de outra sala",
                50,
                RoomStatus.ATIVA
        );

        Room roomFromDataBase = new Room();
        roomFromDataBase.setId(id);
        roomFromDataBase.setRoomName("4-001");
        roomFromDataBase.setCapacity(50);
        roomFromDataBase.setRoomStatus(RoomStatus.ATIVA);

        when(roomRepository.findById(id)).thenReturn(Optional.of(roomFromDataBase));
        when(roomRepository.existsByRoomName(request.name())).thenReturn(true);

        // ACT & ASSERT
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                roomService.updateRoom(id, request));
        assertEquals("Já existe uma sala cadastrada com este nome", exception.getMessage());
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    @DisplayName("Deve deletar sala quando o id existir e estiver ativa")
    void shouldDeleteRoom() {
        // ARRANGE
        Long id = 1L;
        Room roomFromDataBase = new Room();
        roomFromDataBase.setId(id);
        roomFromDataBase.setRoomStatus(RoomStatus.ATIVA);

        when(roomRepository.findById(id)).thenReturn(Optional.of(roomFromDataBase));

        // ACT
        roomService.deactivateRoomById(id);

        // ASSERT
        assertEquals(RoomStatus.INATIVA, roomFromDataBase.getRoomStatus());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar desativar sala com id inexistente")
    void shouldThrowExceptionWhenAttemptingToDeleteAndIdNotFound() {
        // ARRANGE
        Long idNotFound = 99L;
        Room roomFromDataBase = new Room();
        roomFromDataBase.setId(idNotFound);
        roomFromDataBase.setRoomStatus(RoomStatus.ATIVA);

        when(roomRepository.findById(idNotFound)).thenReturn(Optional.empty());

        // ACT & ASSERT
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                roomService.deactivateRoomById(idNotFound));
        assertEquals("Sala não encontrada com o ID: 99", exception.getMessage());
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar desativar sala com status já inativa")
    void shouldThrowExceptionWhenRoomIsAlreadyDeactivated() {
        // ARRANGE
        Long id = 1L;
        Room roomFromDataBase = new Room();
        roomFromDataBase.setId(id);
        roomFromDataBase.setRoomStatus(RoomStatus.INATIVA);

        when(roomRepository.findById(id)).thenReturn(Optional.of(roomFromDataBase));

        // ACT & ASSERT
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                roomService.deactivateRoomById(id));
        assertEquals("Sala já está inativa", exception.getMessage());
        verify(roomRepository, never()).save(any(Room.class));
    }

}
