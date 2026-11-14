package com.roadReady.roadready_backend.dto;

import java.math.BigDecimal;

public record CancellationEligibilityDto(
        Long reservationId,
        String reservationStatus,
        boolean eligible,
        BigDecimal totalAmount,
        BigDecimal refundAmount,
        int refundPercentage,
        String policyReason
) {}