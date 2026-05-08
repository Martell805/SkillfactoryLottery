package ru.vovandiya.dto;

import java.time.LocalDateTime;

public record CreateDrawRequest(
        String format,
        Boolean isInstantaneous,
        Boolean isScheduled,
        LocalDateTime drawDate,
        Integer prisePool,
        Integer ticketCount
) {
}