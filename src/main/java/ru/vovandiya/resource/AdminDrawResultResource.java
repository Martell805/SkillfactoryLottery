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
import ru.vovandiya.dto.DrawResultRequest;
import ru.vovandiya.service.DrawResultService;
import ru.vovandiya.util.ResponseUtil;

@Path("/admin/draw-results")
@RolesAllowed("admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminDrawResultResource {

    @Inject
    DrawResultService drawResultService;

    @Inject
    ResponseUtil responseUtil;

    @GET
    public Response listAll() {
        return responseUtil.execute(() -> Response.ok(drawResultService.listAll()).build());
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return responseUtil.execute(() -> Response.ok(drawResultService.findById(id)).build());
    }

    @POST
    @Transactional
    public Response create(DrawResultRequest request) {
        return responseUtil.execute(() -> Response.status(Response.Status.CREATED).entity(drawResultService.create(request)).build());
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, DrawResultRequest request) {
        return responseUtil.execute(() -> Response.ok(drawResultService.update(id, request)).build());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        return responseUtil.execute(() -> {
            drawResultService.delete(id);
            return Response.noContent().build();
        });
    }
}
