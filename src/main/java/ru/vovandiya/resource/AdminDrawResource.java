package ru.vovandiya.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.vovandiya.dto.DrawRequest;
import ru.vovandiya.service.DrawService;
import ru.vovandiya.util.ResponseUtil;

@RolesAllowed("admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/admin/draws")
public class AdminDrawResource {

    @Inject
    ResponseUtil responseUtil;
    
    @Inject
    DrawService drawService;

    @GET
    public Response listAll() {
        return responseUtil.execute(() -> Response.ok(drawService.listAll()).build());
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return responseUtil.execute(() -> Response.ok(drawService.findById(id)).build());
    }

    @POST
    @Transactional
    public Response create(DrawRequest request) {
        return responseUtil.execute(() -> Response.status(Response.Status.CREATED).entity(drawService.create(request)).build());
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, DrawRequest request) {
        return responseUtil.execute(() -> Response.ok(drawService.update(id, request)).build());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        return responseUtil.execute(() -> {
            drawService.delete(id);
            return Response.noContent().build();
        });
    }
}