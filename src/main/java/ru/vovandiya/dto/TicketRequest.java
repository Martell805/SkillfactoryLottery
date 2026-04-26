package ru.vovandiya.dto;

public record TicketRequest(
        Long drawId,
        Long operationId,
        String pickedNumbers,
        Integer prize
) {}
