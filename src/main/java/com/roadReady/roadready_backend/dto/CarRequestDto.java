package com.roadReady.roadready_backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CarRequestDto(

        @NotBlank(message = "Car name is required")
        @Size(max = 50, message = "Car name cannot exceed 50 chars")
        String carName,

        @Size(max = 50, message = "Brand cannot exceed 50 chars")
        String brand,

        @Size(max = 50, message = "Model cannot exceed 50 chars")
        String model,

        String fuelType,

        @Min(value = 1, message = "Seating capacity must be at least 1")
        Integer seatingCapacity,

        @NotNull(message = "Daily rate is required")
        @Positive(message = "Daily rate must be positive")
        BigDecimal dailyRate,

        String status,
        String licensePlate
) {}