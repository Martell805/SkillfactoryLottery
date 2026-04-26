package ru.vovandiya.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import ru.vovandiya.dto.DrawResultRequest;
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
        return (DrawResult) DrawResult.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("DrawResult not found", 404));
    }

    @Transactional
    public DrawResult create(DrawResultRequest request) {
        DrawResult result = DrawResult.builder()
                .draw(drawService.findById(request.drawId()))
                .drawnNumbers(request.drawnNumbers())
                .status(request.status())
                .build();
        result.persist();
        return result;
    }

    @Transactional
    public DrawResult update(Long id, DrawResultRequest request) {
        DrawResult result = findById(id);
        result.setDraw(drawService.findById(request.drawId()));
        result.setDrawnNumbers(request.drawnNumbers());
        result.setStatus(request.status());
        return result;
    }

    @Transactional
    public void delete(Long id) {
        DrawResult result = findById(id);
        result.delete();
    }
}
