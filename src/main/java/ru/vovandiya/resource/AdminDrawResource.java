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
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.vovandiya.dto.DrawRequest;
import ru.vovandiya.service.DrawService;

import java.util.Map;
import java.util.function.Supplier;

@Path("/admin/draws")
@RolesAllowed("admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
import ru.vovandiya.dto.lottery.CreateDrawRequest;
import ru.vovandiya.service.lottery.DrawService;

@Path("/admin/draws")
public class AdminDrawResource {

    @Inject
    DrawService drawService;

    @GET
    public Response listAll() {
        return execute(() -> Response.ok(drawService.listAll()).build());
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        return execute(() -> Response.ok(drawService.findById(id)).build());
    }

    @POST
    @Transactional
    public Response create(DrawRequest request) {
        return execute(() -> Response.status(Response.Status.CREATED).entity(drawService.create(request)).build());
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, DrawRequest request) {
        return execute(() -> Response.ok(drawService.update(id, request)).build());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        return execute(() -> {
            drawService.delete(id);
            return Response.noContent().build();
        });
    }

    private Response execute(Supplier<Response> action) {
        try {
            return action.get();
        } catch (WebApplicationException exception) {
            return toErrorResponse(exception);
        } catch (RuntimeException exception) {
            return Response.serverError()
                    .type(MediaType.APPLICATION_JSON)
                    .entity(Map.of("error", "Unexpected server error"))
                    .build();
        }
    }

    private Response toErrorResponse(WebApplicationException exception) {
        Response originalResponse = exception.getResponse();
        int status = originalResponse != null ? originalResponse.getStatus() : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        Object entity = originalResponse != null ? originalResponse.getEntity() : null;
        String message = entity instanceof String stringEntity && !stringEntity.isBlank()
                ? stringEntity
                : exception.getMessage();
        if (message == null || message.isBlank()) {
            message = "Request failed";
        }
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", message))
                .build();
    }

    @POST
    public Response createDraw(CreateDrawRequest request) {
        return Response.ok(drawService.createDraw(request)).build();
    }

    @POST
    @Path("/daily")
    public Response createDailyDraw() {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("Daily draw is created by scheduler")
                .build();
    }

    @GET
    @Path("/{drawId}")
    public Response getDraw(@PathParam("drawId") Long drawId) {
        return Response.ok(drawService.getDraw(drawId)).build();
    }
}