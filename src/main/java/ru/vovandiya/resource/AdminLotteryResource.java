package ru.vovandiya.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import ru.vovandiya.service.LotteryService;

@Path("/admin/draws")
public class AdminLotteryResource {

    @Inject
    LotteryService lotteryService;

    @POST
    @Path("/{drawId}/draw-next")
    public Response drawNext(@PathParam("drawId") Long drawId) {
        return Response.ok(lotteryService.drawNextBarrel(drawId)).build();
    }

    @POST
    @Path("/{drawId}/draw-remaining")
    public Response drawRemaining(@PathParam("drawId") Long drawId) {
        return Response.ok(lotteryService.drawRemainingBarrels(drawId)).build();
    }
}