package com.roadReady.roadready_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReservationResponseDto(
        Long reservationId,
        Long customerId,
        String customerUsername,
        Long carId,
        String carName,
        String carBrand,
        String pickupLocation,    // <--- NEW
        String dropoffLocation,
        LocalDate pickupDate,
        LocalTime pickupTime,
        LocalDate dropoffDate,
        Integer numPassengers,
        BigDecimal totalAmount,
        BigDecimal penaltyFee,
        String status,
        LocalDateTime createdAt,
        long durationDays
) {}