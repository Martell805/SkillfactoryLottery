package ru.vovandiya.service;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.vovandiya.dto.ExampleDto;

@ApplicationScoped
public class ExampleService {
    @Inject
    SecurityIdentity securityIdentity;

    public String hello() {
        return "Hello from Quarkus REST to %s".formatted(securityIdentity.getPrincipal().getName());
    }

    public String helloAdmin(ExampleDto exampleDto) {
        return "Hello from Quarkus REST to admin! %s users was banned!".formatted(exampleDto.number());
    }
}
