package ru.vovandiya.dto;

import java.time.LocalDateTime;

public record OperationRequest(
        Long userId,
        LocalDateTime timestamp
) {}
