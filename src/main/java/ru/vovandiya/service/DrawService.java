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
        return (Draw) Draw.findByIdOptional(id)
                .orElseThrow(() -> new WebApplicationException("Draw not found", 404));
    }

    @Transactional
    public Draw create(DrawRequest request) {
        Draw draw = Draw.builder()
                .format(request.format())
                .isInstantaneous(request.isInstantaneous())
                .isScheduled(request.isScheduled())
                .drawDate(request.drawDate())
                .prisePool(request.prisePool())
                .build();
        draw.persist();
        return draw;
    }

    @Transactional
    public Draw update(Long id, DrawRequest request) {
        Draw draw = findById(id);
        draw.setFormat(request.format());
        draw.setIsInstantaneous(request.isInstantaneous());
        draw.setIsScheduled(request.isScheduled());
        draw.setDrawDate(request.drawDate());
        draw.setPrisePool(request.prisePool());
        return draw;
    }

    @Transactional
    public void delete(Long id) {
        Draw draw = findById(id);
        draw.delete();
    }
}
