package ru.vovandiya.dto;

public record DrawResultRequest(
        Long drawId,
        String drawnNumbers,
        DrawStatus status
) {}
