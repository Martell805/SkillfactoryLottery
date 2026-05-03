package ru.vovandiya.dto.lottery;

import ru.vovandiya.dto.DrawStatus;

import java.util.List;

public class DrawRemainingResponse {
    public Long drawId;
    public List<Integer> newNumbers;
    public List<Integer> drawnNumbers;
    public DrawStatus status;

    public DrawRemainingResponse(
            Long drawId,
            List<Integer> newNumbers,
            List<Integer> drawnNumbers,
            DrawStatus status
    ) {
        this.drawId = drawId;
        this.newNumbers = newNumbers;
        this.drawnNumbers = drawnNumbers;
        this.status = status;
    }
}