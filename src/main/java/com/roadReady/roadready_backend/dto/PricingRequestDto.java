package com.roadReady.roadready_backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record PricingRequestDto(
        @NotNull(message = "Car ID is required")
        Long carId,

        @NotNull(message = "Pickup date is required")
        LocalDate pickupDate,

        @NotNull(message = "Dropoff date is required")
        LocalDate dropoffDate
) {}