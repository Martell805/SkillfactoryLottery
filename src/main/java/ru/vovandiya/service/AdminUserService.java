package ru.vovandiya.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import ru.vovandiya.dto.UserRequest;
import ru.vovandiya.model.User;

import java.util.List;

@ApplicationScoped
public class AdminUserService {

    public List<User> listAll() {
        return User.listAll();
    }

    public User findById(Long id) {
        return (User) User.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("User not found", 404));
    }

    @Transactional
    public User create(UserRequest request) {
        if (User.findByUsername(request.username()).isPresent()) {
            throw new WebApplicationException("User already exists", 409);
        }
        User user = User.builder()
                .username(request.username())
                .password(BcryptUtil.bcryptHash(request.password()))
                .role(request.role())
                .build();
        user.persist();
        return user;
    }

    @Transactional
    public User update(Long id, UserRequest request) {
        User user = findById(id);
        user.setUsername(request.username());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(BcryptUtil.bcryptHash(request.password()));
        }
        user.setRole(request.role());
        return user;
    }

    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        user.delete();
    }
}
