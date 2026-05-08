package ru.vovandiya.dto.lottery;

import java.time.LocalDateTime;

public record CreateDrawRequest(
        String format,
        Boolean isInstantaneous,
        Boolean isScheduled,
        LocalDateTime drawDate,
        Integer prisePool,
        /**
         * Сколько билетов автоматически создать при создании тиража.
         * Если null или 0 — билеты автоматически не создаются.
         */
        Integer ticketsCount
) {
}