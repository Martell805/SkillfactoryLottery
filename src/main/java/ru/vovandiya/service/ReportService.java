package ru.vovandiya.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import ru.vovandiya.dto.DrawStatus;
import ru.vovandiya.model.Draw;
import ru.vovandiya.model.DrawResult;
import ru.vovandiya.model.Operation;
import ru.vovandiya.model.Ticket;
import ru.vovandiya.model.User;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ReportService {

    // =========================================================
    // ======================= OPERATIONS ======================
    // =========================================================
    public List<Map<String, Object>> getOperations(Long userId, Long drawId, LocalDateTime from, LocalDateTime to) {
        List<Operation> operations = queryOperations(userId, drawId, from, to);
        List<Map<String, Object>> result = new ArrayList<>(operations.size());
        for (Operation o : operations) {
            result.add(operationToMap(o));
        }
        return result;
    }

    public String getOperationsCsv(Long userId, Long drawId,
            LocalDateTime from, LocalDateTime to) {
        List<Operation> operations = queryOperations(userId, drawId, from, to);
        StringBuilder sb = new StringBuilder("id,userId,username,timestamp\n");
        for (Operation o : operations) {
            User u = o.getUser();
            sb.append(csvValue(o.id)).append(',')
                    .append(csvValue(u != null ? u.id : null)).append(',')
                    .append(csvValue(u != null ? u.getUsername() : null)).append(',')
                    .append(csvValue(o.getTimestamp())).append('\n');
        }
        return sb.toString();
    }

    private List<Operation> queryOperations(Long userId, Long drawId, LocalDateTime from, LocalDateTime to) {
        validateRange("from", from, "to", to);
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (userId != null) {
            conditions.add("user.id = :userId");
            params.put("userId", userId);
        }
        if (drawId != null) {
            conditions.add("EXISTS (SELECT 1 FROM Ticket t "
                    + "WHERE t.operation = _o AND t.draw.id = :drawId)");
            params.put("drawId", drawId);
        }
        if (from != null) {
            conditions.add("timestamp >= :from");
            params.put("from", from);
        }
        if (to != null) {
            conditions.add("timestamp <= :to");
            params.put("to", to);
        }

        // Panache подставит FROM Operation _o, поэтому используем явный алиас _o
        // Но Panache-short-form не даёт задать алиас — используем full form:
        String where = String.join(" AND ", conditions);
        String jpql = conditions.isEmpty() ? "FROM Operation _o ORDER BY _o.timestamp, _o.id"
                : "FROM Operation _o WHERE " + where + " ORDER BY _o.timestamp, _o.id";

        return Operation.<Operation>find(jpql, params).list();
    }

    // =========================================================
    // ======================== TICKETS ========================
    // =========================================================
    public List<Map<String, Object>> getTickets(Long userId, Long drawId,
            LocalDateTime purchasedFrom, LocalDateTime purchasedTo,
            LocalDateTime drawnFrom, LocalDateTime drawnTo) {
        List<Ticket> tickets = queryTickets(userId, drawId, purchasedFrom, purchasedTo, drawnFrom, drawnTo);
        List<Map<String, Object>> result = new ArrayList<>(tickets.size());
        for (Ticket t : tickets) {
            result.add(ticketToMap(t));
        }
        return result;
    }

    public String getTicketsCsv(Long userId, Long drawId,
            LocalDateTime purchasedFrom, LocalDateTime purchasedTo,
            LocalDateTime drawnFrom, LocalDateTime drawnTo) {
        List<Ticket> tickets = queryTickets(userId, drawId, purchasedFrom, purchasedTo, drawnFrom, drawnTo);
        StringBuilder sb = new StringBuilder("id,drawId,drawDate,operationId,userId,username,purchasedAt,pickedNumbers,prize\n");
        for (Ticket t : tickets) {
            Draw d = t.getDraw();
            Operation op = t.getOperation();
            User u = op != null ? op.getUser() : null;
            sb.append(csvValue(t.id)).append(',')
                    .append(csvValue(d != null ? d.id : null)).append(',')
                    .append(csvValue(d != null ? d.getDrawDate() : null)).append(',')
                    .append(csvValue(op != null ? op.id : null)).append(',')
                    .append(csvValue(u != null ? u.id : null)).append(',')
                    .append(csvValue(u != null ? u.getUsername() : null)).append(',')
                    .append(csvValue(op != null ? op.getTimestamp() : null)).append(',')
                    .append(csvValue(t.getPickedNumbers())).append(',')
                    .append(csvValue(t.getPrize())).append('\n');
        }
        return sb.toString();
    }

    private List<Ticket> queryTickets(Long userId, Long drawId,
            LocalDateTime purchasedFrom, LocalDateTime purchasedTo,
            LocalDateTime drawnFrom, LocalDateTime drawnTo) {
        validateRange("purchasedFrom", purchasedFrom, "purchasedTo", purchasedTo);
        validateRange("drawnFrom", drawnFrom, "drawnTo", drawnTo);
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (userId != null) {
            conditions.add("operation.user.id = :userId");
            params.put("userId", userId);
        }
        if (drawId != null) {
            conditions.add("draw.id = :drawId");
            params.put("drawId", drawId);
        }
        if (purchasedFrom != null) {
            conditions.add("operation.timestamp >= :purchasedFrom");
            params.put("purchasedFrom", purchasedFrom);
        }
        if (purchasedTo != null) {
            conditions.add("operation.timestamp <= :purchasedTo");
            params.put("purchasedTo", purchasedTo);
        }
        if (drawnFrom != null) {
            conditions.add("draw.drawDate >= :drawnFrom");
            params.put("drawnFrom", drawnFrom);
        }
        if (drawnTo != null) {
            conditions.add("draw.drawDate <= :drawnTo");
            params.put("drawnTo", drawnTo);
        }

        String where = String.join(" AND ", conditions);
        String jpql = conditions.isEmpty()
                ? "ORDER BY id"
                : where + " ORDER BY id";

        return Ticket.<Ticket>find(jpql, params).list();
    }

    // =========================================================
    // ========================= DRAWS =========================
    // =========================================================
    public List<Map<String, Object>> getDraws(LocalDateTime from, LocalDateTime to, DrawStatus status, String format) {
        List<Draw> draws = queryDraws(from, to, status, format);
        List<Map<String, Object>> result = new ArrayList<>(draws.size());
        for (Draw d : draws) {
            result.add(drawToMap(d, findDrawResult(d)));
        }
        return result;
    }

    public String getDrawsCsv(LocalDateTime from, LocalDateTime to,
            DrawStatus status, String format) {
        List<Draw> draws = queryDraws(from, to, status, format);
        StringBuilder sb = new StringBuilder(
                "id,format,isInstantaneous,isScheduled,drawDate,prisePool,status,drawnNumbers\n");
        for (Draw d : draws) {
            DrawResult dr = findDrawResult(d);
            sb.append(csvValue(d.id)).append(',')
                    .append(csvValue(d.getFormat())).append(',')
                    .append(csvValue(d.getIsInstantaneous())).append(',')
                    .append(csvValue(d.getIsScheduled())).append(',')
                    .append(csvValue(d.getDrawDate())).append(',')
                    .append(csvValue(d.getPrisePool())).append(',')
                    .append(csvValue(dr != null && dr.getStatus() != null ? dr.getStatus().name() : null)).append(',')
                    .append(csvValue(dr != null ? dr.getDrawnNumbers() : null)).append('\n');
        }
        return sb.toString();
    }

    private List<Draw> queryDraws(LocalDateTime from, LocalDateTime to, DrawStatus status, String format) {
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
        if (status != null) {
            conditions.add("EXISTS (SELECT 1 FROM DrawResult r "
                    + "WHERE r.draw = _d AND r.status = :status)");
            params.put("status", status);
        }

        String where = String.join(" AND ", conditions);
        String jpql = conditions.isEmpty()
                ? "FROM Draw _d ORDER BY _d.drawDate, _d.id"
                : "FROM Draw _d WHERE " + where + " ORDER BY _d.drawDate, _d.id";

        return Draw.<Draw>find(jpql, params).list();
    }

    private DrawResult findDrawResult(Draw draw) {
        return DrawResult.<DrawResult>find("draw", draw).firstResult();
    }

    // =========================================================
    // ======================= MAPPERS =========================
    // =========================================================
    private Map<String, Object> operationToMap(Operation o) {
        Map<String, Object> m = new LinkedHashMap<>();
        User u = o.getUser();
        m.put("id", o.id);
        m.put("userId", u != null ? u.id : null);
        m.put("username", u != null ? u.getUsername() : null);
        m.put("timestamp", o.getTimestamp());
        return m;
    }

    private Map<String, Object> ticketToMap(Ticket t) {
        Map<String, Object> m = new LinkedHashMap<>();
        Draw d = t.getDraw();
        Operation op = t.getOperation();
        User u = op != null ? op.getUser() : null;
        m.put("id", t.id);
        m.put("drawId", d != null ? d.id : null);
        m.put("drawDate", d != null ? d.getDrawDate() : null);
        m.put("operationId", op != null ? op.id : null);
        m.put("userId", u != null ? u.id : null);
        m.put("username", u != null ? u.getUsername() : null);
        m.put("purchasedAt", op != null ? op.getTimestamp() : null);
        m.put("pickedNumbers", t.getPickedNumbers());
        m.put("prize", t.getPrize());
        return m;
    }

    private Map<String, Object> drawToMap(Draw d, DrawResult dr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.id);
        m.put("format", d.getFormat());
        m.put("isInstantaneous", d.getIsInstantaneous());
        m.put("isScheduled", d.getIsScheduled());
        m.put("drawDate", d.getDrawDate());
        m.put("prisePool", d.getPrisePool());
        m.put("status", dr != null ? dr.getStatus() : null);
        m.put("drawnNumbers", dr != null ? dr.getDrawnNumbers() : null);
        return m;
    }

    // =========================================================
    // ======================== UTILS ==========================
    // =========================================================
    private void validateRange(String fromName, LocalDateTime from, String toName, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new WebApplicationException(fromName + " must be before or equal to " + toName, 400);
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
        String s = String.valueOf(value);
        if (s.contains("\"")) {
            s = s.replace("\"", "\"\"");
        }
        if (s.contains(",") || s.contains("\n") || s.contains("\r") || s.contains("\"")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}
