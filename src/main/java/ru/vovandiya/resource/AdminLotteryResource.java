package ru.vovandiya.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import ru.vovandiya.dto.CreateDrawRequest;
import ru.vovandiya.service.LotteryDrawService;
import ru.vovandiya.service.LotteryService;
import ru.vovandiya.util.ResponseUtil;

@Path("/admin/lottery")
@RolesAllowed({"admin"})
public class AdminLotteryResource {

    @Inject
    ResponseUtil responseUtil;
    
    @Inject
    LotteryService lotteryService;

    @Inject
    LotteryDrawService drawService;

    @POST
    @Path("/{drawId}/draw-next")
    public Response drawNext(@PathParam("drawId") Long drawId) {
        return responseUtil.execute(() -> Response.ok(lotteryService.drawNextBarrel(drawId)).build());
    }

    @POST
    @Path("/{drawId}/draw-remaining")
    public Response drawRemaining(@PathParam("drawId") Long drawId) {
        return responseUtil.execute(() -> Response.ok(lotteryService.drawRemainingBarrels(drawId)).build());
    }

    @POST
    @Path("/create")
    public Response createDraw(CreateDrawRequest request) {
        return responseUtil.execute(() -> Response.ok(drawService.createDraw(request)).build());
    }

    @POST
    @Path("/daily")
    public Response createDailyDraw() {
        return responseUtil.execute(() -> Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("Daily draw is created by scheduler")
                .build());
    }

    @GET
    @Path("/{drawId}")
    public Response getDraw(@PathParam("drawId") Long drawId) {
        return responseUtil.execute(() -> Response.ok(drawService.getDraw(drawId)).build());
    }
}