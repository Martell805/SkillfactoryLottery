package ru.vovandiya.service;

import jakarta.enterprise.context.ApplicationScoped;
import ru.vovandiya.dto.DrawStatus;
import ru.vovandiya.model.Draw;
import ru.vovandiya.model.DrawResult;
import ru.vovandiya.model.Operation;
import ru.vovandiya.model.Ticket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReportService {

    // ---- Operations ----

    public List<Operation> getOperations(Long userId, Long drawId, LocalDateTime from, LocalDateTime to) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (userId != null) {
            conditions.add("user.id = :userId");
            params.put("userId", userId);
        }
        if (from != null) {
            conditions.add("timestamp >= :from");
            params.put("from", from);
        }
        if (to != null) {
            conditions.add("timestamp <= :to");
            params.put("to", to);
        }

        List<Operation> operations = conditions.isEmpty()
                ? Operation.listAll()
                : Operation.list(String.join(" AND ", conditions), params);

        if (drawId != null) {
            List<Long> operationIds = Ticket.<Ticket>find("draw.id = :drawId", Map.of("drawId", drawId))
                    .stream()
                    .filter(t -> t.getOperation() != null)
                    .map(t -> t.getOperation().id)
                    .collect(Collectors.toList());
            operations = operations.stream()
                    .filter(o -> operationIds.contains(o.id))
                    .collect(Collectors.toList());
        }

        return operations;
    }

    public String getOperationsCsv(Long userId, Long drawId, LocalDateTime from, LocalDateTime to) {
        List<Operation> operations = getOperations(userId, drawId, from, to);
        StringBuilder sb = new StringBuilder("id,userId,username,timestamp\n");
        for (Operation o : operations) {
            sb.append(o.id).append(',')
              .append(o.getUser().id).append(',')
              .append(o.getUser().getUsername()).append(',')
              .append(o.getTimestamp()).append('\n');
        }
        return sb.toString();
    }

    // ---- Tickets ----

    public List<Ticket> getTickets(Long userId, Long drawId,
                                   LocalDateTime purchasedFrom, LocalDateTime purchasedTo,
                                   LocalDateTime drawnFrom, LocalDateTime drawnTo) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (drawId != null) {
            conditions.add("draw.id = :drawId");
            params.put("drawId", drawId);
        }
        if (drawnFrom != null) {
            conditions.add("draw.drawDate >= :drawnFrom");
            params.put("drawnFrom", drawnFrom);
        }
        if (drawnTo != null) {
            conditions.add("draw.drawDate <= :drawnTo");
            params.put("drawnTo", drawnTo);
        }

        List<Ticket> tickets = conditions.isEmpty()
                ? Ticket.listAll()
                : Ticket.list(String.join(" AND ", conditions), params);

        if (userId != null) {
            tickets = tickets.stream()
                    .filter(t -> t.getOperation() != null && t.getOperation().getUser().id.equals(userId))
                    .collect(Collectors.toList());
        }

        if (purchasedFrom != null || purchasedTo != null) {
            tickets = tickets.stream()
                    .filter(t -> {
                        if (t.getOperation() == null) return false;
                        LocalDateTime ts = t.getOperation().getTimestamp();
                        if (purchasedFrom != null && ts.isBefore(purchasedFrom)) return false;
                        if (purchasedTo != null && ts.isAfter(purchasedTo)) return false;
                        return true;
                    })
                    .collect(Collectors.toList());
        }

        return tickets;
    }

    public String getTicketsCsv(Long userId, Long drawId,
                                LocalDateTime purchasedFrom, LocalDateTime purchasedTo,
                                LocalDateTime drawnFrom, LocalDateTime drawnTo) {
        List<Ticket> tickets = getTickets(userId, drawId, purchasedFrom, purchasedTo, drawnFrom, drawnTo);
        StringBuilder sb = new StringBuilder("id,drawId,operationId,pickedNumbers,prize\n");
        for (Ticket t : tickets) {
            sb.append(t.id).append(',')
              .append(t.getDraw().id).append(',')
              .append(t.getOperation() != null ? t.getOperation().id : "").append(',')
              .append(t.getPickedNumbers() != null ? t.getPickedNumbers() : "").append(',')
              .append(t.getPrize() != null ? t.getPrize() : "").append('\n');
        }
        return sb.toString();
    }

    // ---- Draws ----

    public List<Draw> getDraws(LocalDateTime from, LocalDateTime to, DrawStatus status, String format) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (from != null) {
            conditions.add("drawDate >= :from");
            params.put("from", from);
        }
        if (to != null) {
            conditions.add("drawDate <= :to");
            params.put("to", to);
        }
        if (format != null) {
            conditions.add("format = :format");
            params.put("format", format);
        }

        List<Draw> draws = conditions.isEmpty()
                ? Draw.listAll()
                : Draw.list(String.join(" AND ", conditions), params);

        if (status != null) {
            List<Long> drawIds = DrawResult.<DrawResult>find("status = :status", Map.of("status", status))
                    .stream()
                    .map(dr -> dr.getDraw().id)
                    .collect(Collectors.toList());
            draws = draws.stream()
                    .filter(d -> drawIds.contains(d.id))
                    .collect(Collectors.toList());
        }

        return draws;
    }

    public String getDrawsCsv(LocalDateTime from, LocalDateTime to, DrawStatus status, String format) {
        List<Draw> draws = getDraws(from, to, status, format);
        StringBuilder sb = new StringBuilder("id,format,isInstantaneous,isScheduled,drawDate,prisePool\n");
        for (Draw d : draws) {
            sb.append(d.id).append(',')
              .append(d.getFormat()).append(',')
              .append(d.getIsInstantaneous()).append(',')
              .append(d.getIsScheduled()).append(',')
              .append(d.getDrawDate() != null ? d.getDrawDate() : "").append(',')
              .append(d.getPrisePool() != null ? d.getPrisePool() : "").append('\n');
        }
        return sb.toString();
    }
}
