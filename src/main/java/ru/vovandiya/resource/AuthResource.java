package ru.vovandiya.resource;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.vovandiya.dto.TokenResponse;
import ru.vovandiya.dto.LoginRequest;
import ru.vovandiya.service.AuthService;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/register")
    @Transactional
    public Response register(LoginRequest request) {
        authService.register(request.username(), request.password(), "user");
        return Response.status(201).build();
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        var token = authService.login(request.username(), request.password());
        return Response.ok(new TokenResponse(token)).build();
    }
}