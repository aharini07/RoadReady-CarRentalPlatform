package com.roadReady.roadready_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CancellationResponseDto(
        Long cancellationId,
        Long reservationId,
        LocalDateTime cancellationDate,
        String reason,
        BigDecimal refundAmount,
        String status,
        String reservationStatus   // should be "CANCELLED" after this completes
) {}