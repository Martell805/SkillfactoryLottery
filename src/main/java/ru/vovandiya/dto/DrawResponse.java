package ru.vovandiya.dto.lottery;

import ru.vovandiya.dto.DrawStatus;

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
        Long ticketsCount
) {
}