package com.roadReady.roadready_backend.dto;

import java.time.LocalDateTime;

public record CheckInOutResponseDto(
        Long checkinId,
        Long reservationId,
        Long agentId,
        String agentName,
        String pickupCondition,
        String returnCondition,
        String fuelLevelBefore,
        String fuelLevelAfter,
        LocalDateTime checkinDate,    // pickup time
        LocalDateTime checkoutDate,   // return time
        String remarks
) {}