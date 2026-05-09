package ru.vovandiya.dto;

public record LotteryFormat(
        int minNumber,
        int maxNumber,
        int numbersToDraw
) {
}