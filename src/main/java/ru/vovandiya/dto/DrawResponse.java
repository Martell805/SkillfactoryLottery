package ru.vovandiya.dto.lottery;

import ru.vovandiya.dto.DrawStatus;

import java.time.LocalDateTime;

public class DrawResponse {
    public Long id;
    public String format;
    public Boolean isInstantaneous;
    public Boolean isScheduled;
    public LocalDateTime drawDate;
    public Integer prisePool;
    public DrawStatus status;
    public String drawnNumbers;

    public DrawResponse(
            Long id,
            String format,
            Boolean isInstantaneous,
            Boolean isScheduled,
            LocalDateTime drawDate,
            Integer prisePool,
            DrawStatus status,
            String drawnNumbers
    ) {
        this.id = id;
        this.format = format;
        this.isInstantaneous = isInstantaneous;
        this.isScheduled = isScheduled;
        this.drawDate = drawDate;
        this.prisePool = prisePool;
        this.status = status;
        this.drawnNumbers = drawnNumbers;
    }
}