package ru.vovandiya.service;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import ru.vovandiya.model.User;

import java.time.Duration;

@ApplicationScoped
public class AuthService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @Transactional
    public void register(String username, String password, String role) {
        if (User.findByUsername(username).isPresent()) {
            throw new WebApplicationException("User already exists", 409);
        }

        User.builder()
            .username(username)
            .password(BcryptUtil.bcryptHash(password))
            .role(role)
            .build()
            .persist();
    }

    public String login(String username, String password) {
        var user = User.findByUsername(username)
                .orElseThrow(() -> new WebApplicationException("Invalid credentials", 401));

        if (!BcryptUtil.matches(password, user.getPassword())) {
            throw new WebApplicationException("Invalid credentials", 401);
        }

        return generateToken(user);
    }

    private String generateToken(User user) {
        return Jwt.issuer(issuer)
                .subject(user.getUsername())
                .groups(user.getRole())
                .expiresIn(Duration.ofHours(24))
                .sign();
    }
}