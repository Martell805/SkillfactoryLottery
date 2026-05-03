package ru.vovandiya.dto.lottery;

import ru.vovandiya.dto.DrawStatus;

import java.util.List;

public class DrawNextResponse {
    public Long drawId;
    public Integer drawnNumber;
    public List<Integer> drawnNumbers;
    public DrawStatus status;

    public DrawNextResponse(
            Long drawId,
            Integer drawnNumber,
            List<Integer> drawnNumbers,
            DrawStatus status
    ) {
        this.drawId = drawId;
        this.drawnNumber = drawnNumber;
        this.drawnNumbers = drawnNumbers;
        this.status = status;
    }
}