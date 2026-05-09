package ru.vovandiya.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.vovandiya.dto.CreateDrawRequest;
import ru.vovandiya.dto.DrawResponse;
import ru.vovandiya.dto.DrawStatus;
import ru.vovandiya.dto.LotteryFormat;
import ru.vovandiya.model.Draw;
import ru.vovandiya.model.DrawResult;
import ru.vovandiya.model.Ticket;

import java.time.LocalDateTime;

@ApplicationScoped
public class LotteryDrawService {

    @Inject
    TicketGenerationService ticketGenerationService;

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

        int ticketCount = request.ticketCount() == null ? 0 : request.ticketCount();
        ticketGenerationService.generateTickets(draw, format, ticketCount);

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
            Integer ticketCount
    ) {
        CreateDrawRequest request = new CreateDrawRequest(
                format,
                instantaneous,
                true,
                LocalDateTime.now(),
                prisePool,
                ticketCount
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

        if (request.ticketCount() != null) {
            if (request.ticketCount() < 0) {
                throw new IllegalArgumentException("Tickets count cannot be negative");
            }

            if (request.ticketCount() > MAX_AUTO_GENERATED_TICKETS) {
                throw new IllegalArgumentException("Tickets count is too large");
            }
        }
    }

    private DrawResponse toResponse(Draw draw, DrawResult result) {
        Long ticketCount = Ticket.count("draw", draw);

        return new DrawResponse(
                draw.id,
                draw.getFormat(),
                draw.getIsInstantaneous(),
                draw.getIsScheduled(),
                draw.getDrawDate(),
                draw.getPrisePool(),
                result == null ? null : result.getStatus(),
                result == null ? "" : result.getDrawnNumbers(),
                ticketCount
        );
    }
}

