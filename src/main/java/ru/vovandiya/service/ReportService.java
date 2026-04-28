package ru.vovandiya.service;

import jakarta.enterprise.context.ApplicationScoped;
import ru.vovandiya.dto.DrawStatus;
import ru.vovandiya.model.Draw;
import ru.vovandiya.model.DrawResult;
import ru.vovandiya.model.Operation;
import ru.vovandiya.model.Ticket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReportService {

    // ---- Operations ----

    public List<Operation> getOperations(Long userId, Long drawId, LocalDateTime from, LocalDateTime to) {
        validateRange("from", from, "to", to);
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
            Set<Long> operationIds = Ticket.<Ticket>find("draw.id = :drawId", Map.of("drawId", drawId))
                    .stream()
                    .map(Ticket::getOperation)
                    .filter(Objects::nonNull)
                    .map(operation -> operation.id)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (operationIds.isEmpty()) {
                return List.of();
            }
            operations = operations.stream()
                    .filter(operation -> operation != null && operation.id != null && operationIds.contains(operation.id))
                    .collect(Collectors.toList());
        }

        return operations.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Operation::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(operation -> operation.id, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public String getOperationsCsv(Long userId, Long drawId, LocalDateTime from, LocalDateTime to) {
        List<Operation> operations = getOperations(userId, drawId, from, to);
        StringBuilder sb = new StringBuilder("id,userId,username,timestamp\n");
        for (Operation o : operations) {
            sb.append(csvValue(o.id)).append(',')
              .append(csvValue(o.getUser() != null ? o.getUser().id : null)).append(',')
              .append(csvValue(o.getUser() != null ? o.getUser().getUsername() : null)).append(',')
              .append(csvValue(o.getTimestamp())).append('\n');
        }
        return sb.toString();
    }

    // ---- Tickets ----

    public List<Ticket> getTickets(Long userId, Long drawId,
                                   LocalDateTime purchasedFrom, LocalDateTime purchasedTo,
                                   LocalDateTime drawnFrom, LocalDateTime drawnTo) {
        validateRange("purchasedFrom", purchasedFrom, "purchasedTo", purchasedTo);
        validateRange("drawnFrom", drawnFrom, "drawnTo", drawnTo);
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
                    .filter(ticket -> ticket.getOperation() != null
                            && ticket.getOperation().getUser() != null
                            && Objects.equals(ticket.getOperation().getUser().id, userId))
                    .collect(Collectors.toList());
        }

        if (purchasedFrom != null || purchasedTo != null) {
            tickets = tickets.stream()
                    .filter(ticket -> {
                        if (ticket.getOperation() == null) {
                            return false;
                        }
                        LocalDateTime timestamp = ticket.getOperation().getTimestamp();
                        if (timestamp == null) {
                            return false;
                        }
                        if (purchasedFrom != null && timestamp.isBefore(purchasedFrom)) {
                            return false;
                        }
                        if (purchasedTo != null && timestamp.isAfter(purchasedTo)) {
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
        }

        return tickets.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing((Ticket ticket) -> ticket.id, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public String getTicketsCsv(Long userId, Long drawId,
                                LocalDateTime purchasedFrom, LocalDateTime purchasedTo,
                                LocalDateTime drawnFrom, LocalDateTime drawnTo) {
        List<Ticket> tickets = getTickets(userId, drawId, purchasedFrom, purchasedTo, drawnFrom, drawnTo);
        StringBuilder sb = new StringBuilder("id,drawId,operationId,pickedNumbers,prize\n");
        for (Ticket t : tickets) {
            sb.append(csvValue(t.id)).append(',')
              .append(csvValue(t.getDraw() != null ? t.getDraw().id : null)).append(',')
              .append(csvValue(t.getOperation() != null ? t.getOperation().id : null)).append(',')
              .append(csvValue(t.getPickedNumbers())).append(',')
              .append(csvValue(t.getPrize())).append('\n');
        }
        return sb.toString();
    }

    // ---- Draws ----

    public List<Draw> getDraws(LocalDateTime from, LocalDateTime to, DrawStatus status, String format) {
        validateRange("from", from, "to", to);
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
        String normalizedFormat = normalizeFilterValue(format);
        if (normalizedFormat != null) {
            conditions.add("format = :format");
            params.put("format", normalizedFormat);
        }

        List<Draw> draws = conditions.isEmpty()
                ? Draw.listAll()
                : Draw.list(String.join(" AND ", conditions), params);

        if (status != null) {
            Set<Long> drawIds = DrawResult.<DrawResult>find("status = :status", Map.of("status", status))
                    .stream()
                    .map(DrawResult::getDraw)
                    .filter(Objects::nonNull)
                    .map(draw -> draw.id)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (drawIds.isEmpty()) {
                return List.of();
            }
            draws = draws.stream()
                    .filter(draw -> draw != null && draw.id != null && drawIds.contains(draw.id))
                    .collect(Collectors.toList());
        }

        return draws.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Draw::getDrawDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(draw -> draw.id, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public String getDrawsCsv(LocalDateTime from, LocalDateTime to, DrawStatus status, String format) {
        List<Draw> draws = getDraws(from, to, status, format);
        StringBuilder sb = new StringBuilder("id,format,isInstantaneous,isScheduled,drawDate,prisePool\n");
        for (Draw d : draws) {
            sb.append(csvValue(d.id)).append(',')
              .append(csvValue(d.getFormat())).append(',')
              .append(csvValue(d.getIsInstantaneous())).append(',')
              .append(csvValue(d.getIsScheduled())).append(',')
              .append(csvValue(d.getDrawDate())).append(',')
              .append(csvValue(d.getPrisePool())).append('\n');
        }
        return sb.toString();
    }

    private void validateRange(String fromName, LocalDateTime from, String toName, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new jakarta.ws.rs.WebApplicationException(fromName + " must be before or equal to " + toName, 400);
        }
    }

    private String normalizeFilterValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "";
        }
        String stringValue = String.valueOf(value);
        if (stringValue.contains("\"")) {
            stringValue = stringValue.replace("\"", "\"\"");
        }
        if (stringValue.contains(",") || stringValue.contains("\n") || stringValue.contains("\r")) {
            return "\"" + stringValue + "\"";
        }
        return stringValue;
    }
}
