package ru.vovandiya.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.vovandiya.dto.UserRequest;
import ru.vovandiya.service.AdminUserService;
import ru.vovandiya.util.ResponseUtil;

@Path("/admin/users")
@RolesAllowed("admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminUserResource {

    @Inject
    ResponseUtil responseUtil;

    @Inject
    AdminUserService adminUserService;

    @GET
    public Response listAll() {
        return responseUtil.execute(() -> Response.ok(adminUserService.listAll()).build());
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return responseUtil.execute(() -> Response.ok(adminUserService.findById(id)).build());
    }

    @POST
    @Transactional
    public Response create(UserRequest request) {
        return responseUtil.execute(() -> Response.status(Response.Status.CREATED).entity(adminUserService.create(request)).build());
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, UserRequest request) {
        return responseUtil.execute(() -> Response.ok(adminUserService.update(id, request)).build());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        return responseUtil.execute(() -> {
            adminUserService.delete(id);
            return Response.noContent().build();
        });
    }
}
