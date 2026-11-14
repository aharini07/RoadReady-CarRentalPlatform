package com.roadReady.roadready_backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReturnRequestDto(

        @NotBlank(message = "Return condition is required")
        @Size(max = 255, message = "Return condition cannot exceed 255 characters")
        String returnCondition,

        @NotBlank(message = "Fuel level after return is required")
        @Size(max = 50, message = "Fuel level cannot exceed 50 characters")
        String fuelLevelAfter,

        @Size(max = 255, message = "Remarks cannot exceed 255 characters")
        String remarks,

        // NEW FIELDS FOR PENALTY
        BigDecimal penaltyFee,

        String damageDescription
) {}