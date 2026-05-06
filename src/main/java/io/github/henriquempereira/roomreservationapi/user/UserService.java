package io.github.henriquempereira.roomreservationapi.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    @Transactional
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

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    public UserResponse getUserById(Long id) {
        User user = getUserOrThrow(id);
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = getUserOrThrow(id);
        user.setUserName(request.name());

        User userSaved = repository.save(user);

        return toResponse(userSaved);
    }

    @Transactional
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
