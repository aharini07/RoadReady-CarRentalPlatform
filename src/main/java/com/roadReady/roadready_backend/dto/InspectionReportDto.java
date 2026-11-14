package com.roadReady.roadready_backend.dto;

import java.time.LocalDateTime;

/**
 * Combined view of pickup vs return. Used by GET /report.
 * Computes damage flag by string-comparing conditions — real systems
 * would use structured fields, but this is honest about what your schema
 * supports today.
 */
public record InspectionReportDto(
        Long reservationId,
        Long carId,
        String carName,
        String customerUsername,
        String reservationStatus,

        String pickupCondition,
        String returnCondition,
        boolean damageReported,

        String fuelLevelBefore,
        String fuelLevelAfter,
        boolean fuelShortage,

        Long agentId,
        String agentName,
        LocalDateTime pickupTime,
        LocalDateTime returnTime,

        String pickupRemarks,
        String returnRemarks
) {}