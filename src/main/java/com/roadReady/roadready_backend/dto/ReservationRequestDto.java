package com.roadReady.roadready_backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationRequestDto(

        @NotNull(message = "Car ID is required")
        Long carId,
        @NotBlank(message = "Pickup location is required")
        String pickupLocation,

        @NotBlank(message = "Dropoff location is required")
        String dropoffLocation,

        @NotNull(message = "Pickup date is required")
        @FutureOrPresent(message = "Pickup date cannot be in the past")
        LocalDate pickupDate,

        @NotNull(message = "Pickup time is required")
        LocalTime pickupTime,

        @NotNull(message = "Dropoff date is required")
        LocalDate dropoffDate,

        @Min(value = 1, message = "Number of passengers must be at least 1")
        Integer numPassengers
) {}