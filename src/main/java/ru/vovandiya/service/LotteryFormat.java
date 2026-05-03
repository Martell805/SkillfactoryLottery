package ru.vovandiya.service.lottery;

public record LotteryFormat(
        int minNumber,
        int maxNumber,
        int numbersToDraw
) {
}