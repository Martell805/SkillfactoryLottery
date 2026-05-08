package ru.vovandiya.dto;

import java.util.List;

public record DrawRemainingResponse(
        Long drawId,
        List<Integer> newNumbers,
        List<Integer> drawnNumbers,
        DrawStatus status
) {
}