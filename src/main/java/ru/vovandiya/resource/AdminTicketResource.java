package ru.vovandiya.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.vovandiya.dto.TicketRequest;
import ru.vovandiya.service.TicketService;
import ru.vovandiya.util.ResponseUtil;

@Path("/admin/tickets")
@RolesAllowed("admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminTicketResource {

    @Inject
    ResponseUtil responseUtil;

    @Inject
    TicketService ticketService;

    @GET
    public Response listAll() {
        return responseUtil.execute(() -> Response.ok(ticketService.listAll()).build());
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return responseUtil.execute(() -> Response.ok(ticketService.findById(id)).build());
    }

    @POST
    @Transactional
    public Response create(TicketRequest request) {
        return responseUtil.execute(() -> Response.status(Response.Status.CREATED).entity(ticketService.create(request)).build());
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, TicketRequest request) {
        return responseUtil.execute(() -> Response.ok(ticketService.update(id, request)).build());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        return responseUtil.execute(() -> {
            ticketService.delete(id);
            return Response.noContent().build();
        });
    }
}
