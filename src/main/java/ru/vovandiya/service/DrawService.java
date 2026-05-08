package ru.vovandiya.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import ru.vovandiya.dto.DrawRequest;
import ru.vovandiya.model.Draw;
import java.util.List;
import jakarta.inject.Inject;
import ru.vovandiya.dto.DrawStatus;
import ru.vovandiya.dto.lottery.CreateDrawRequest;
import ru.vovandiya.dto.lottery.DrawResponse;
import ru.vovandiya.model.DrawResult;
import ru.vovandiya.model.Ticket;
import java.time.LocalDateTime;

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

    @Inject
    LotteryFormatService lotteryFormatService;

    @Transactional
    public DrawResponse createDraw(CreateDrawRequest request) {
        validateCreateDrawRequest(request);

        LotteryFormat format = lotteryFormatService.parse(request.format());

        Draw draw = Draw.builder()
                .format(request.format())
                .isInstantaneous(Boolean.TRUE.equals(request.isInstantaneous()))
                .isScheduled(Boolean.TRUE.equals(request.isScheduled()))
                .drawDate(request.drawDate())
                .prisePool(request.prisePool())
                .build();

        draw.persist();

        DrawResult result = DrawResult.builder()
                .draw(draw)
                .drawnNumbers("")
                .status(DrawStatus.NEW)
                .build();

        result.persist();

        int ticketsCount = request.ticketsCount == null ? 0 : request.ticketsCount;
        ticketGenerationService.generateTickets(draw, format, ticketsCount);

        return toResponse(draw, result);
    }

    @Transactional
    public DrawResponse createDailyDraw(String format, Integer prisePool, Boolean instantaneous) {
        CreateDrawRequest request = new CreateDrawRequest(
                format,
                instantaneous,
                true,
                LocalDateTime.now(),
                prisePool,
                0
        );

        return createDraw(request);
    }
    @Transactional
    public DrawResponse createDailyDraw(
            String format,
            Integer prisePool,
            Boolean instantaneous,
            Integer ticketsCount
    ) {
        CreateDrawRequest request = new CreateDrawRequest(
                format,
                instantaneous,
                true,
                LocalDateTime.now(),
                prisePool,
                ticketsCount
        );

        return createDraw(request);
    }

    public DrawResponse getDraw(Long drawId) {
        Draw draw = Draw.findById(drawId);

        if (draw == null) {
            throw new IllegalArgumentException("Draw not found: " + drawId);
        }

        DrawResult result = DrawResult.find("draw", draw).firstResult();

        return toResponse(draw, result);
    }

    private static final int MAX_AUTO_GENERATED_TICKETS = 10_000;

    private void validateCreateDrawRequest(CreateDrawRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        lotteryFormatService.parse(request.format());

        if (request.prisePool() == null || request.prisePool() <= 0) {
            throw new IllegalArgumentException("Prise pool must be positive");
        }

        if (request.ticketsCount() != null) {
            if (request.ticketsCount() < 0) {
                throw new IllegalArgumentException("Tickets count cannot be negative");
            }

            if (request.ticketsCount() > MAX_AUTO_GENERATED_TICKETS) {
                throw new IllegalArgumentException("Tickets count is too large");
            }
        }
    }
    private DrawResponse toResponse(Draw draw, DrawResult result) {
        Long ticketsCount = Ticket.count("draw", draw);

        return new DrawResponse(
                draw.id,
                draw.getFormat(),
                draw.getIsInstantaneous(),
                draw.getIsScheduled(),
                draw.getDrawDate(),
                draw.getPrisePool(),
                result == null ? null : result.getStatus(),
                result == null ? "" : result.getDrawnNumbers(),
                ticketsCount
        );
    }
}