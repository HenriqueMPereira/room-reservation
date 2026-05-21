package io.github.henriquempereira.roomreservationapi.user;

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
 * Suíte de testes unitários para a classe {@link UserService}.
 * <p>
 * Valida as regras de negócio de criação, atualização e exclusão de usuários,
 * assegurando o funcionamento correto do ciclo de vida da entidade sem acesso real ao banco de dados.
 * </p>
 * <p>
 * Principais cenários cobertos:
 * <ul>
 * <li>Criação de usuários assegurando a regra de negócio de unicidade de CPF.</li>
 * <li>Atualização de dados cadastrais condicionada à pré-existência do registro.</li>
 * <li>Exclusão física (hard delete) do usuário.</li>
 * <li>Geração de exceções com mensagens descritivas para operações em IDs inexistentes.</li>
 * </ul>
 * </p>
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock // Cria um dublê
    private UserRepository userRepository;

    @InjectMocks // Cria o Service de verdade mas injeta o "dublê"
    private UserService userService;

    @Test
    @DisplayName("Deve criar um usuário com sucesso quando o CPF não existir no banco")
    void shouldCreateUserSuccessfully() {
        // ARRANGE
        // Criamos os dados fictícios que o Postman mandaria
        UserRequest request = new UserRequest(
                "Henrique Morey Pereira",
                "123.456.789-00"
        );

        // Criamos a entidade simulando como ela voltaria do banco de dados (já com o ID gerado)
        User userMockFromDatabase = new User();
        userMockFromDatabase.setId(1L);
        userMockFromDatabase.setUserName("Henrique Pereira");
        userMockFromDatabase.setCpf("123.456.789-00");

        // Aqui ensinamos o dublê: "Quando o service perguntar se esse CPF existe, responda FALSE"
        when(userRepository.existsByCpf(request.cpf())).thenReturn(false);

        // Ensinamos o dublê: "Quando o service pedir para salvar qualquer usuário, devolva o nosso userMockFromDatabase"
        when(userRepository.save(any(User.class))).thenReturn(userMockFromDatabase);

        // ACT
        UserResponse response = userService.createUser(request);

        // ASSERT
        assertNotNull(response);
        assertEquals("Henrique Pereira", response.name());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar usuário com CPF que já existe")
    void shouldThrowExceptionWhenCpfAlreadyExists() {
        // ARRANGE
        UserRequest request = new UserRequest(
                "Henrique Pereira",
                "123.456.789-00"
        );

        when(userRepository.existsByCpf(request.cpf())).thenReturn(true);

        // ACT && ASSERT
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                userService.createUser(request));

        assertEquals("Já existe usuário cadastrado com este CPF!", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve atualizar um usuário com sucesso quando o ID existir")
    void shouldUpdateUserSuccessfully() {
        // ARRANGE
        Long id = 1L;
        UserRequest userRequest = new UserRequest(
                "Henrique Pereira",
                "009.876.543-21"
        );

        // Simula como os dados estão no banco
        User userMockFromDataBase = new User();
        userMockFromDataBase.setId(id);
        userMockFromDataBase.setUserName("Nome Antigo");
        userMockFromDataBase.setCpf("123.456.789-00");

        // Simula como os dados ficarão no banco
        User updatedUser = new User();
        updatedUser.setId(id);
        updatedUser.setUserName("Henrique Pereira");
        updatedUser.setCpf("009.876.543-21");

        // Ensinando o dublê a devolver um Optional existente
        when(userRepository.findById(id)).thenReturn(Optional.of(userMockFromDataBase));

        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // ACT
        UserResponse userResponse = userService.updateUser(id, userRequest);

        // ASSERT
        assertNotNull(userResponse);
        assertEquals("Henrique Pereira", userResponse.name());
        assertEquals("009.876.543-21", userResponse.cpf());

        // Verificamos se o dublê foi chamado para buscar e para salvar
        verify(userRepository, times(1)).findById(id);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar um usuário com ID inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Arrange
        Long idNotFound = 99L;
        UserRequest userRequest = new UserRequest(
                "Ghost",
                "000.000.000-00"
        );

        when(userRepository.findById(idNotFound)).thenReturn(Optional.empty());

        // ACT & ASSERT
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                userService.updateUser(idNotFound, userRequest));

        assertEquals("Usuário não encontrado com o ID: 99", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve deletar um usuário com sucesso quando o ID existir")
    void shouldDeleteUserSuccessfully() {
        //ARRANGE
        Long id = 1L;
        User userMockFromDataBase = new User();
        userMockFromDataBase.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(userMockFromDataBase));

        // ACT
        userService.deleteUser(id);

        // ASSERT
        verify(userRepository, times(1)).delete(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar um usuário que não existe")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // ARRANGE
        Long idNotFound = 99L;

        when(userRepository.findById(idNotFound)).thenReturn(Optional.empty());

        // ACT & ASSERT
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                userService.deleteUser(idNotFound));

        assertEquals("Usuário não encontrado com o ID: 99", exception.getMessage());

        verify(userRepository, never()).delete(any(User.class));
    }
}
