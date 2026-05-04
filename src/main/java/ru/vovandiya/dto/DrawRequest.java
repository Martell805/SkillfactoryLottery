package ru.vovandiya.dto;

import java.time.LocalDateTime;

public record DrawRequest(
        String format,
        Boolean isInstantaneous,
        Boolean isScheduled,
        LocalDateTime drawDate,
        Integer prisePool
) {}
