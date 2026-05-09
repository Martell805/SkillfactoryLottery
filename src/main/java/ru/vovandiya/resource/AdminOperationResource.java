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
import ru.vovandiya.dto.OperationRequest;
import ru.vovandiya.service.OperationService;
import ru.vovandiya.util.ResponseUtil;

@Path("/admin/operations")
@RolesAllowed("admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminOperationResource {

    @Inject
    OperationService operationService;

    @Inject
    ResponseUtil responseUtil;

    @GET
    public Response listAll() {
        return responseUtil.execute(() -> Response.ok(operationService.listAll()).build());
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return responseUtil.execute(() -> Response.ok(operationService.findById(id)).build());
    }

    @POST
    @Transactional
    public Response create(OperationRequest request) {
        return responseUtil.execute(() -> Response.status(Response.Status.CREATED).entity(operationService.create(request)).build());
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, OperationRequest request) {
        return responseUtil.execute(() -> Response.ok(operationService.update(id, request)).build());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        return responseUtil.execute(() -> {
            operationService.delete(id);
            return Response.noContent().build();
        });
    }
}
