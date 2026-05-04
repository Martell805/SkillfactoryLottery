package ru.vovandiya.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import ru.vovandiya.dto.DrawResultRequest;
import ru.vovandiya.dto.DrawStatus;
import ru.vovandiya.model.DrawResult;

import java.util.List;

@ApplicationScoped
public class DrawResultService {

    @Inject
    DrawService drawService;

    public List<DrawResult> listAll() {
        return DrawResult.listAll();
    }

    public DrawResult findById(Long id) {
        requirePositiveId(id, "id");
        return (DrawResult) DrawResult.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("DrawResult not found", 404));
    }

    @Transactional
    public DrawResult create(DrawResultRequest request) {
        validateRequest(request);
        DrawResult result = DrawResult.builder()
                .draw(drawService.findById(requirePositiveId(request.drawId(), "drawId")))
                .drawnNumbers(normalizeOptionalText(request.drawnNumbers()))
                .status(requireStatus(request.status()))
                .build();
        result.persist();
        return result;
    }

    @Transactional
    public DrawResult update(Long id, DrawResultRequest request) {
        validateRequest(request);
        DrawResult result = findById(id);
        result.setDraw(drawService.findById(requirePositiveId(request.drawId(), "drawId")));
        result.setDrawnNumbers(normalizeOptionalText(request.drawnNumbers()));
        result.setStatus(requireStatus(request.status()));
        return result;
    }

    @Transactional
    public void delete(Long id) {
        DrawResult result = findById(id);
        result.delete();
    }

    private void validateRequest(DrawResultRequest request) {
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

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private DrawStatus requireStatus(DrawStatus status) {
        if (status == null) {
            throw new WebApplicationException("status is required", 400);
        }
        return status;
    }
}
