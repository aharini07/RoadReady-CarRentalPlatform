package com.roadReady.roadready_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDto(
        Long paymentId,
        Long reservationId,
        BigDecimal amount,
        String paymentMethod,
        LocalDateTime paymentDate,
        String paymentStatus,
        String transactionId
) {}