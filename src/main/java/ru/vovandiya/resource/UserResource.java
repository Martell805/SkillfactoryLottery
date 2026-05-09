package ru.vovandiya.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import ru.vovandiya.dto.BuyNewTicketRequest;
import ru.vovandiya.dto.DrawStatus;
import ru.vovandiya.model.Ticket;
import ru.vovandiya.model.User;
import ru.vovandiya.service.UserDrawService;
import ru.vovandiya.service.UserOperationService;
import ru.vovandiya.service.UserTicketService;
import ru.vovandiya.util.ResponseUtil;

import java.time.LocalDateTime;

@Path("/user")
@RolesAllowed({"user", "admin"})
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    ResponseUtil responseUtil;

    @Inject
    UserDrawService userDrawService;

    @Inject
    UserTicketService userTicketService;

    @Inject
    UserOperationService userOperationService;

    /**
     * GET /user/draws?status=NEW&format=bingo&from=...&to=...
     * Получение всех тиражей с фильтрами.
     */
    @GET
    @Path("/draws")
    public Response listDraws(
            @QueryParam("status") DrawStatus status,
            @QueryParam("format") String format,
            @QueryParam("from") LocalDateTime from,
            @QueryParam("to") LocalDateTime to) {
        return responseUtil.execute(() -> Response.ok(userDrawService.listDraws(status, format, from, to)).build());
    }

    /**
     * GET /user/draws/{drawId}/result
     * Получить весь результат тиража.
     */
    @GET
    @Path("/draws/{drawId}/result")
    public Response getDrawResult(@PathParam("drawId") Long drawId) {
        return responseUtil.execute(() -> Response.ok(userDrawService.getDrawResult(drawId)).build());
    }

    /**
     * GET /user/draws/{drawId}/last-barrel
     * Получить последний бочонок тиража.
     */
    @GET
    @Path("/draws/{drawId}/last-barrel")
    public Response getLastBarrel(@PathParam("drawId") Long drawId) {
        return responseUtil.execute(() -> Response.ok(userDrawService.getLastBarrel(drawId)).build());
    }

    /**
     * GET /user/draws/{drawId}/tickets/available
     * Получение всех непроданных билетов по тиражу.
     */
    @GET
    @Path("/draws/{drawId}/tickets/available")
    public Response getAvailableTickets(@PathParam("drawId") Long drawId) {
        return responseUtil.execute(() -> Response.ok(userDrawService.getAvailableTickets(drawId)).build());
    }

    /**
     * POST /user/tickets/{ticketId}/buy
     * Покупка подготовленного заранее билета по id.
     */
    @POST
    @Path("/tickets/{ticketId}/buy")
    @Transactional
    public Response buyExistingTicket(
            @PathParam("ticketId") Long ticketId,
            @Context SecurityContext securityContext) {
        return responseUtil.execute(() -> {
            User currentUser = resolveCurrentUser(securityContext);
            Ticket ticket = userTicketService.buyExistingTicket(ticketId, currentUser);
            return Response.ok(ticket).build();
        });
    }

    /**
     * POST /user/draws/{drawId}/tickets/buy
     * Покупка билета с созданием в рамках тиража.
     * Body: { "pickedNumbers": "5,12,33" }
     */
    @POST
    @Path("/draws/{drawId}/tickets/buy")
    @Transactional
    public Response buyNewTicket(
            @PathParam("drawId") Long drawId,
            BuyNewTicketRequest request,
            @Context SecurityContext securityContext) {
        return responseUtil.execute(() -> {
            User currentUser = resolveCurrentUser(securityContext);
            String pickedNumbers = request != null ? request.pickedNumbers() : null;
            Ticket ticket = userTicketService.buyNewTicket(drawId, pickedNumbers, currentUser);
            return Response.status(Response.Status.CREATED).entity(ticket).build();
        });
    }

    /**
     * GET /user/me/tickets?drawId=1&from=...&to=...
     * Получить свои билеты за определённое время и за определённый тираж.
     */
    @GET
    @Path("/me/tickets")
    public Response getMyTickets(
            @QueryParam("drawId") Long drawId,
            @QueryParam("from") LocalDateTime from,
            @QueryParam("to") LocalDateTime to,
            @Context SecurityContext securityContext) {
        return responseUtil.execute(() -> {
            User currentUser = resolveCurrentUser(securityContext);
            return Response.ok(userTicketService.getMyTickets(currentUser, drawId, from, to)).build();
        });
    }

    /**
     * GET /user/me/operations?from=...&to=...
     * Получить историю своих операций за определённое время.
     */
    @GET
    @Path("/me/operations")
    public Response getMyOperations(
            @QueryParam("from") LocalDateTime from,
            @QueryParam("to") LocalDateTime to,
            @Context SecurityContext securityContext) {
        return responseUtil.execute(() -> {
            User currentUser = resolveCurrentUser(securityContext);
            return Response.ok(userOperationService.getMyOperations(currentUser, from, to)).build();
        });
    }

    private User resolveCurrentUser(SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            throw new WebApplicationException("Unauthorized", 401);
        }
        String username = securityContext.getUserPrincipal().getName();
        return User.findByUsername(username)
                .orElseThrow(() -> new WebApplicationException("User not found", 404));
    }
}
