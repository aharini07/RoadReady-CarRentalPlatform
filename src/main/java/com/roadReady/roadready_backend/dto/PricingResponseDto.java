package com.roadReady.roadready_backend.dto;

import java.math.BigDecimal;

public record PricingResponseDto(
        Long carId,
        String carName,
        BigDecimal dailyRate,
        long durationDays,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal totalAmount
) {}