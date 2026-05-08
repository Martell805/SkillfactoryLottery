package ru.vovandiya.dto.lottery;

import ru.vovandiya.dto.DrawStatus;

import java.util.List;

public record DrawRemainingResponse(
        Long drawId,
        List<Integer> newNumbers,
        List<Integer> drawnNumbers,
        DrawStatus status
) {
}