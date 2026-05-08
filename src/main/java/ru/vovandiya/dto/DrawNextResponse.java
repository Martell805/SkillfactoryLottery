package ru.vovandiya.dto.lottery;

import ru.vovandiya.dto.DrawStatus;

import java.util.List;

public record DrawNextResponse(
        Long drawId,
        Integer drawnNumber,
        List<Integer> drawnNumbers,
        DrawStatus status
) {
}