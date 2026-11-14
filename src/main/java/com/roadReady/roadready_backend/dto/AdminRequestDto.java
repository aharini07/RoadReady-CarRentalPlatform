package com.roadReady.roadready_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminRequestDto(

        @NotNull(message = "User ID is required")
        Long userId,

        @NotBlank(message = "Admin name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @Size(max = 15, message = "Phone number must not exceed 15 characters")
        String phoneNo,

        @Size(max = 50, message = "Department must not exceed 50 characters")
        String department
) {}
