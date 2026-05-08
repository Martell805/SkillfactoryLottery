package ru.vovandiya.dto;

import java.util.List;

public record DrawNextResponse(
        Long drawId,
        Integer drawnNumber,
        List<Integer> drawnNumbers,
        DrawStatus status
) {
}