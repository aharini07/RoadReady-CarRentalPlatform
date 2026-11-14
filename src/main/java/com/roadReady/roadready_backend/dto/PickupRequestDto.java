package com.roadReady.roadready_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PickupRequestDto(

        @NotBlank(message = "Pickup condition is required")
        @Size(max = 255, message = "Pickup condition cannot exceed 255 characters")
        String pickupCondition,

        @NotBlank(message = "Fuel level before pickup is required")
        @Size(max = 50, message = "Fuel level cannot exceed 50 characters")
        String fuelLevelBefore,

        @Size(max = 255, message = "Remarks cannot exceed 255 characters")
        String remarks
) {}