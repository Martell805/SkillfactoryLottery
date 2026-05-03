package ru.vovandiya.service.lottery;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.vovandiya.dto.DrawStatus;
import ru.vovandiya.dto.lottery.DrawNextResponse;
import ru.vovandiya.dto.lottery.DrawRemainingResponse;
import ru.vovandiya.model.Draw;
import ru.vovandiya.model.DrawResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ApplicationScoped
public class LotteryService {

    @Inject
    LotteryFormatService formatService;

    @Inject
    LotteryNumbersService numbersService;

    @Inject
    PrizeCalculationService prizeCalculationService;

    @Transactional
    public DrawNextResponse drawNextBarrel(Long drawId) {
        Draw draw = findDraw(drawId);
        DrawResult result = findDrawResult(draw);

        if (result.getStatus() == DrawStatus.COMPLETE) {
            throw new IllegalStateException("Draw is already complete");
        }

        LotteryFormat format = formatService.parse(draw.getFormat());
        List<Integer> drawnNumbers = new ArrayList<>(numbersService.parseNumbers(result.getDrawnNumbers()));

        if (drawnNumbers.size() >= format.numbersToDraw()) {
            completeDraw(draw, result, drawnNumbers);

            return new DrawNextResponse(
                    draw.id,
                    null,
                    drawnNumbers,
                    result.getStatus()
            );
        }

        if (result.getStatus() == DrawStatus.NEW) {
            result.setStatus(DrawStatus.STARTED);
        }

        Integer nextNumber = getRandomAvailableNumber(format, drawnNumbers);
        drawnNumbers.add(nextNumber);

        result.setDrawnNumbers(numbersService.toString(drawnNumbers));

        if (drawnNumbers.size() >= format.numbersToDraw()) {
            completeDraw(draw, result, drawnNumbers);
        }

        return new DrawNextResponse(
                draw.id,
                nextNumber,
                drawnNumbers,
                result.getStatus()
        );
    }

    @Transactional
    public DrawRemainingResponse drawRemainingBarrels(Long drawId) {
        Draw draw = findDraw(drawId);
        DrawResult result = findDrawResult(draw);

        if (result.getStatus() == DrawStatus.COMPLETE) {
            return new DrawRemainingResponse(
                    draw.id,
                    List.of(),
                    numbersService.parseNumbers(result.getDrawnNumbers()),
                    result.getStatus()
            );
        }

        LotteryFormat format = formatService.parse(draw.getFormat());
        List<Integer> drawnNumbers = new ArrayList<>(numbersService.parseNumbers(result.getDrawnNumbers()));
        List<Integer> newNumbers = new ArrayList<>();

        if (result.getStatus() == DrawStatus.NEW) {
            result.setStatus(DrawStatus.STARTED);
        }

        while (drawnNumbers.size() < format.numbersToDraw()) {
            Integer nextNumber = getRandomAvailableNumber(format, drawnNumbers);
            drawnNumbers.add(nextNumber);
            newNumbers.add(nextNumber);
        }

        result.setDrawnNumbers(numbersService.toString(drawnNumbers));
        completeDraw(draw, result, drawnNumbers);

        return new DrawRemainingResponse(
                draw.id,
                newNumbers,
                drawnNumbers,
                result.getStatus()
        );
    }

    private void completeDraw(Draw draw, DrawResult result, List<Integer> drawnNumbers) {
        result.setStatus(DrawStatus.COMPLETE);
        result.setDrawnNumbers(numbersService.toString(drawnNumbers));
        prizeCalculationService.calculatePrizes(draw, drawnNumbers);
    }

    private Integer getRandomAvailableNumber(LotteryFormat format, List<Integer> drawnNumbers) {
        List<Integer> available = new ArrayList<>();

        for (int number = format.minNumber(); number <= format.maxNumber(); number++) {
            if (!drawnNumbers.contains(number)) {
                available.add(number);
            }
        }

        if (available.isEmpty()) {
            throw new IllegalStateException("No available numbers left");
        }

        int index = ThreadLocalRandom.current().nextInt(available.size());
        return available.get(index);
    }

    private Draw findDraw(Long drawId) {
        Draw draw = Draw.findById(drawId);

        if (draw == null) {
            throw new IllegalArgumentException("Draw not found: " + drawId);
        }

        return draw;
    }

    private DrawResult findDrawResult(Draw draw) {
        DrawResult result = DrawResult.find("draw", draw).firstResult();

        if (result == null) {
            throw new IllegalStateException("Draw result not found for draw: " + draw.id);
        }

        return result;
    }
}