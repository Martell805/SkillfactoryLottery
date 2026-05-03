package ru.vovandiya.dto.lottery;

import java.time.LocalDateTime;

public class CreateDrawRequest {
    public String format;
    public Boolean isInstantaneous;
    public Boolean isScheduled;
    public LocalDateTime drawDate;
    public Integer prisePool;
}