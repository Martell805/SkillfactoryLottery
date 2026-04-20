package ru.vovandiya.resource;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import ru.vovandiya.dto.ExampleDto;
import ru.vovandiya.service.ExampleService;

@Path("/example")
public class ExampleResource {
    @Inject
    ExampleService exampleService;

    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public String hello() {
        return exampleService.hello();
    }

    @GET
    @Path("/secret")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("admin")
    public String helloAdmin(ExampleDto exampleDto) {
        return exampleService.helloAdmin(exampleDto);
    }
}
