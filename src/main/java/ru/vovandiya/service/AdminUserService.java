package ru.vovandiya.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import ru.vovandiya.dto.UserRequest;
import ru.vovandiya.model.User;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class AdminUserService {

    public List<User> listAll() {
        return User.listAll();
    }

    public User findById(Long id) {
        requirePositiveId(id, "id");
        return (User) User.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("User not found", 404));
    }

    @Transactional
    public User create(UserRequest request) {
        validateRequest(request);
        String username = normalizeRequiredText(request.username(), "username");
        String role = normalizeRequiredText(request.role(), "role");
        String password = requirePassword(request.password(), true);
        validateUniqueUsername(username, null);
        User user = User.builder()
                .username(username)
                .password(BcryptUtil.bcryptHash(password))
                .role(role)
                .build();
        user.persist();
        return user;
    }

    @Transactional
    public User update(Long id, UserRequest request) {
        validateRequest(request);
        User user = findById(id);
        String username = normalizeRequiredText(request.username(), "username");
        String role = normalizeRequiredText(request.role(), "role");
        validateUniqueUsername(username, id);
        user.setUsername(username);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(BcryptUtil.bcryptHash(requirePassword(request.password(), false)));
        }
        user.setRole(role);
        return user;
    }

    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        user.delete();
    }

    private void validateRequest(UserRequest request) {
        if (request == null) {
            throw new WebApplicationException("Request body is required", 400);
        }
    }

    private void validateUniqueUsername(String username, Long currentUserId) {
        User.findByUsername(username).ifPresent(existingUser -> {
            if (currentUserId == null || !Objects.equals(existingUser.id, currentUserId)) {
                throw new WebApplicationException("User already exists", 409);
            }
        });
    }

    private Long requirePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new WebApplicationException(fieldName + " must be positive", 400);
        }
        return id;
    }

    private String normalizeRequiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new WebApplicationException(fieldName + " is required", 400);
        }
        return value.trim();
    }

    private String requirePassword(String password, boolean required) {
        if (password == null) {
            if (required) {
                throw new WebApplicationException("password is required", 400);
            }
            return null;
        }
        if (password.isBlank()) {
            throw new WebApplicationException("password must not be blank", 400);
        }
        return password;
    }
}
