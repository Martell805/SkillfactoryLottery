package ru.vovandiya.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import ru.vovandiya.dto.DrawRequest;
import ru.vovandiya.model.Draw;

import java.util.List;

@ApplicationScoped
public class DrawService {

    public List<Draw> listAll() {
        return Draw.listAll();
    }

    public Draw findById(Long id) {
        requirePositiveId(id, "id");
        return (Draw) Draw.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("Draw not found", 404));
    }

    @Transactional
    public Draw create(DrawRequest request) {
        validateRequest(request);
        String format = normalizeRequiredText(request.format(), "format");
        Boolean isInstantaneous = requireBoolean(request.isInstantaneous(), "isInstantaneous");
        Boolean isScheduled = requireBoolean(request.isScheduled(), "isScheduled");
        validateSchedule(isInstantaneous, isScheduled, request.drawDate());
        Integer prisePool = requireNonNegative(request.prisePool(), "prisePool");
        Draw draw = Draw.builder()
                .format(format)
                .isInstantaneous(isInstantaneous)
                .isScheduled(isScheduled)
                .drawDate(request.drawDate())
                .prisePool(prisePool)
                .build();
        draw.persist();
        return draw;
    }

    @Transactional
    public Draw update(Long id, DrawRequest request) {
        validateRequest(request);
        Draw draw = findById(id);
        String format = normalizeRequiredText(request.format(), "format");
        Boolean isInstantaneous = requireBoolean(request.isInstantaneous(), "isInstantaneous");
        Boolean isScheduled = requireBoolean(request.isScheduled(), "isScheduled");
        validateSchedule(isInstantaneous, isScheduled, request.drawDate());
        Integer prisePool = requireNonNegative(request.prisePool(), "prisePool");
        draw.setFormat(format);
        draw.setIsInstantaneous(isInstantaneous);
        draw.setIsScheduled(isScheduled);
        draw.setDrawDate(request.drawDate());
        draw.setPrisePool(prisePool);
        return draw;
    }

    @Transactional
    public void delete(Long id) {
        Draw draw = findById(id);
        draw.delete();
    }

    private void validateRequest(DrawRequest request) {
        if (request == null) {
            throw new WebApplicationException("Request body is required", 400);
        }
    }

    private Long requirePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new WebApplicationException(fieldName + " must be positive", 400);
        }
        return id;
    }

    private String normalizeRequiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new WebApplicationException(fieldName + " is required", 400);
        }
        return value.trim();
    }

    private Boolean requireBoolean(Boolean value, String fieldName) {
        if (value == null) {
            throw new WebApplicationException(fieldName + " is required", 400);
        }
        return value;
    }

    private Integer requireNonNegative(Integer value, String fieldName) {
        if (value == null) {
            throw new WebApplicationException(fieldName + " is required", 400);
        }
        if (value < 0) {
            throw new WebApplicationException(fieldName + " must not be negative", 400);
        }
        return value;
    }

    private void validateSchedule(Boolean isInstantaneous, Boolean isScheduled, Object drawDate) {
        if (!Boolean.TRUE.equals(isInstantaneous) && !Boolean.TRUE.equals(isScheduled)) {
            throw new WebApplicationException("At least one draw mode must be enabled", 400);
        }
        if (Boolean.TRUE.equals(isScheduled) && drawDate == null) {
            throw new WebApplicationException("drawDate is required for scheduled draws", 400);
        }
    }
}
