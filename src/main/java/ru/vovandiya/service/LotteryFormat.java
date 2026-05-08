package ru.vovandiya.service;

public record LotteryFormat(
        int minNumber,
        int maxNumber,
        int numbersToDraw
) {
}