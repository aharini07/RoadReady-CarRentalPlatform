package com.roadReady.roadready_backend.dto;

import java.math.BigDecimal;

public record CarResponseDto(
        Long carId,
        String carName,
        String brand,
        String model,
        String fuelType,
        Integer seatingCapacity,
        BigDecimal dailyRate,
        String status,
        String licensePlate
) {}