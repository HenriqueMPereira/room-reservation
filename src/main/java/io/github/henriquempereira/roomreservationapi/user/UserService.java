package io.github.henriquempereira.roomreservationapi.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public UserResponse createUser(UserRequest request) {
        if(repository.existsByCpf(request.cpf())){
            throw new IllegalArgumentException("Já existe usuário cadastrado com este CPF!");
        }
        User user = new User();
        user.setUserName(request.name());
        user.setCpf(request.cpf());

        User userSaved = repository.save(user);
        return toResponse(userSaved);
    }

    public List<UserResponse> getAllUsers() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = getUserOrThrow(id);
        return toResponse(user);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        User user = getUserOrThrow(id);
        user.setUserName(request.name());

        User userSaved = repository.save(user);

        return toResponse(userSaved);
    }

    public void deleteUser(Long id) {
        User user = getUserOrThrow(id);
        repository.delete(user);
    }

    private User getUserOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com o ID: " + id));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getCpf()
        );
    }
}
