package ru.vovandiya.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.vovandiya.dto.CreateDrawRequest;
import ru.vovandiya.service.LotteryDrawService;
import ru.vovandiya.service.LotteryService;

import java.util.Map;
import java.util.function.Supplier;

@Path("/admin/draws")
public class AdminLotteryResource {

    @Inject
    LotteryService lotteryService;

    @Inject
    LotteryDrawService drawService;

    @POST
    @Path("/{drawId}/draw-next")
    public Response drawNext(@PathParam("drawId") Long drawId) {
        return execute(() -> Response.ok(lotteryService.drawNextBarrel(drawId)).build());
    }

    @POST
    @Path("/{drawId}/draw-remaining")
    public Response drawRemaining(@PathParam("drawId") Long drawId) {
        return execute(() -> Response.ok(lotteryService.drawRemainingBarrels(drawId)).build());
    }

    @POST
    @Path("/create")
    public Response createDraw(CreateDrawRequest request) {
        return execute(() -> Response.ok(drawService.createDraw(request)).build());
    }

    @POST
    @Path("/daily")
    public Response createDailyDraw() {
        return execute(() -> Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("Daily draw is created by scheduler")
                .build());
    }

    @GET
    @Path("/{drawId}")
    public Response getDraw(@PathParam("drawId") Long drawId) {
        return execute(() -> Response.ok(drawService.getDraw(drawId)).build());
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
}