package ru.vovandiya.service.lottery;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LotteryFormatService {

    public LotteryFormat parse(String format) {
        if (format == null || format.isBlank()) {
            throw new IllegalArgumentException("Draw format is required");
        }

        String[] rangeAndCount = format.trim().split(":");

        if (rangeAndCount.length != 2) {
            throw new IllegalArgumentException("Draw format must be like 1-90:5");
        }

        String[] range = rangeAndCount[0].split("-");

        if (range.length != 2) {
            throw new IllegalArgumentException("Draw format range must be like 1-90");
        }

        int minNumber = parsePositiveInt(range[0], "min number");
        int maxNumber = parsePositiveInt(range[1], "max number");
        int numbersToDraw = parsePositiveInt(rangeAndCount[1], "numbers to draw");

        if (minNumber >= maxNumber) {
            throw new IllegalArgumentException("Min number must be less than max number");
        }

        int availableNumbers = maxNumber - minNumber + 1;

        if (numbersToDraw > availableNumbers) {
            throw new IllegalArgumentException("Numbers to draw cannot be greater than available numbers");
        }

        return new LotteryFormat(minNumber, maxNumber, numbersToDraw);
    }

    private int parsePositiveInt(String value, String fieldName) {
        try {
            int number = Integer.parseInt(value.trim());

            if (number <= 0) {
                throw new IllegalArgumentException(fieldName + " must be positive");
            }

            return number;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a number");
        }
    }
}