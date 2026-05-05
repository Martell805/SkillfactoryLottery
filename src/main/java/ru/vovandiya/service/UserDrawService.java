package ru.vovandiya.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import ru.vovandiya.dto.DrawStatus;
import ru.vovandiya.model.Draw;
import ru.vovandiya.model.DrawResult;
import ru.vovandiya.model.Ticket;

import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class UserDrawService {

    /**
     * Получение всех тиражей с фильтрами по статусу, формату и времени проведения.
     */
    public List<Map<String, Object>> listDraws(DrawStatus status, String format,
                                               LocalDateTime from, LocalDateTime to) {
        validateRange(from, to);
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (format != null && !format.isBlank()) {
            conditions.add("format = :format");
            params.put("format", format.trim());
        }
        if (from != null) {
            conditions.add("drawDate >= :from");
            params.put("from", from);
        }
        if (to != null) {
            conditions.add("drawDate <= :to");
            params.put("to", to);
        }
        if (status != null) {
            conditions.add("EXISTS (SELECT 1 FROM DrawResult r WHERE r.draw = _d AND r.status = :status)");
            params.put("status", status);
        }

        String where = String.join(" AND ", conditions);
        String jpql = conditions.isEmpty()
                ? "FROM Draw _d ORDER BY _d.drawDate, _d.id"
                : "FROM Draw _d WHERE " + where + " ORDER BY _d.drawDate, _d.id";

        List<Draw> draws = Draw.<Draw>find(jpql, params).list();

        List<Map<String, Object>> result = new ArrayList<>(draws.size());
        for (Draw d : draws) {
            DrawResult dr = DrawResult.<DrawResult>find("draw", d).firstResult();
            result.add(drawToMap(d, dr));
        }
        return result;
    }

    /**
     * Получить весь результат тиража (drawnNumbers + статус).
     */
    public Map<String, Object> getDrawResult(Long drawId) {
        Draw draw = findDrawById(drawId);
        DrawResult dr = DrawResult.<DrawResult>find("draw", draw).firstResult();
        if (dr == null) {
            throw new WebApplicationException("Draw result not found for draw " + drawId, 404);
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("drawId", draw.id);
        map.put("format", draw.getFormat());
        map.put("drawDate", draw.getDrawDate());
        map.put("prisePool", draw.getPrisePool());
        map.put("status", dr.getStatus());
        map.put("drawnNumbers", dr.getDrawnNumbers());
        return map;
    }

    /**
     * Получить последний бочонок тиража — последнее число из drawnNumbers.
     */
    public Map<String, Object> getLastBarrel(Long drawId) {
        Draw draw = findDrawById(drawId);
        DrawResult dr = DrawResult.<DrawResult>find("draw", draw).firstResult();
        if (dr == null || dr.getDrawnNumbers() == null || dr.getDrawnNumbers().isBlank()) {
            throw new WebApplicationException("No drawn numbers available for draw " + drawId, 404);
        }
        String[] parts = dr.getDrawnNumbers().trim().split("[,\\s]+");
        String lastBarrel = parts[parts.length - 1].trim();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("drawId", draw.id);
        map.put("lastBarrel", lastBarrel);
        map.put("totalDrawn", parts.length);
        map.put("status", dr.getStatus());
        return map;
    }

    /**
     * Получить все непроданные билеты по тиражу
     * (билеты без привязанной операции — не куплены пользователями).
     */
    public List<Ticket> getAvailableTickets(Long drawId) {
        Draw draw = findDrawById(drawId);
        return Ticket.<Ticket>find("draw = ?1 AND operation IS NULL", draw).list();
    }

    private Draw findDrawById(Long drawId) {
        if (drawId == null || drawId <= 0) {
            throw new WebApplicationException("drawId must be positive", 400);
        }
        return (Draw) Draw.findByIdOptional(drawId)
                .orElseThrow(() -> new WebApplicationException("Draw not found", 404));
    }

    private void validateRange(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new WebApplicationException("'from' must be before or equal to 'to'", 400);
        }
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
}
