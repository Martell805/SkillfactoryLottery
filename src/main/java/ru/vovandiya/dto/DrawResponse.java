package ru.vovandiya.dto;

import java.time.LocalDateTime;

public record DrawResponse(
        Long id,
        String format,
        Boolean isInstantaneous,
        Boolean isScheduled,
        LocalDateTime drawDate,
        Integer prisePool,
        DrawStatus status,
        String drawnNumbers,
        Long ticketCount
) {
}