package ru.vovandiya.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.vovandiya.dto.DrawStatus;
import ru.vovandiya.service.ReportService;
import ru.vovandiya.util.ResponseUtil;

import java.time.LocalDateTime;

@Path("/admin/reports")
@RolesAllowed("admin")
public class AdminReportResource {

    @Inject
    ReportService reportService;

    @Inject
    ResponseUtil responseUtil;

    // ---- Operations ----

    @GET
    @Path("/operations/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response operationsJson(
            @QueryParam("userId") Long userId,
            @QueryParam("drawId") Long drawId,
            @QueryParam("from") LocalDateTime from,
            @QueryParam("to") LocalDateTime to) {
        return responseUtil.execute(() -> Response.ok(reportService.getOperations(userId, drawId, from, to)).build());
    }

    @GET
    @Path("/operations/csv")
    @Produces("text/csv")
    public Response operationsCsv(
            @QueryParam("userId") Long userId,
            @QueryParam("drawId") Long drawId,
            @QueryParam("from") LocalDateTime from,
            @QueryParam("to") LocalDateTime to) {
        return responseUtil.execute(() -> {
            String csv = reportService.getOperationsCsv(userId, drawId, from, to);
            return Response.ok(csv)
                    .header("Content-Disposition", "attachment; filename=operations.csv")
                    .build();
        });
    }

    // ---- Tickets ----

    @GET
    @Path("/tickets/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ticketsJson(
            @QueryParam("userId") Long userId,
            @QueryParam("drawId") Long drawId,
            @QueryParam("purchasedFrom") LocalDateTime purchasedFrom,
            @QueryParam("purchasedTo") LocalDateTime purchasedTo,
            @QueryParam("drawnFrom") LocalDateTime drawnFrom,
            @QueryParam("drawnTo") LocalDateTime drawnTo) {
        return responseUtil.execute(() -> Response.ok(reportService.getTickets(
                userId, drawId, purchasedFrom, purchasedTo, drawnFrom, drawnTo)).build());
    }

    @GET
    @Path("/tickets/csv")
    @Produces("text/csv")
    public Response ticketsCsv(
            @QueryParam("userId") Long userId,
            @QueryParam("drawId") Long drawId,
            @QueryParam("purchasedFrom") LocalDateTime purchasedFrom,
            @QueryParam("purchasedTo") LocalDateTime purchasedTo,
            @QueryParam("drawnFrom") LocalDateTime drawnFrom,
            @QueryParam("drawnTo") LocalDateTime drawnTo) {
        return responseUtil.execute(() -> {
            String csv = reportService.getTicketsCsv(
                    userId, drawId, purchasedFrom, purchasedTo, drawnFrom, drawnTo);
            return Response.ok(csv)
                    .header("Content-Disposition", "attachment; filename=tickets.csv")
                    .build();
        });
    }

    // ---- Draws ----

    @GET
    @Path("/draws/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Response drawsJson(
            @QueryParam("from") LocalDateTime from,
            @QueryParam("to") LocalDateTime to,
            @QueryParam("status") DrawStatus status,
            @QueryParam("format") String format) {
        return responseUtil.execute(() -> Response.ok(reportService.getDraws(from, to, status, format)).build());
    }

    @GET
    @Path("/draws/csv")
    @Produces("text/csv")
    public Response drawsCsv(
            @QueryParam("from") LocalDateTime from,
            @QueryParam("to") LocalDateTime to,
            @QueryParam("status") DrawStatus status,
            @QueryParam("format") String format) {
        return responseUtil.execute(() -> {
            String csv = reportService.getDrawsCsv(from, to, status, format);
            return Response.ok(csv)
                    .header("Content-Disposition", "attachment; filename=draws.csv")
                    .build();
        });
    }
}
