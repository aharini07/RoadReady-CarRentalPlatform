
package com.roadReady.roadready_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RentalAgentDto(

        @NotBlank(message = "Username is required")
        @Size(min = 4, max = 30,
                message = "Username must be between 4 and 30 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72,
                message = "Password must be between 8 and 72 characters")
        String password,

        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name cannot exceed 100 characters")
        String name,

        @Pattern(regexp = "^[0-9]{10}$",
                message = "Phone must contain exactly 10 digits")
        String phone,

        @NotBlank(message = "Shift is required")
        String shift

) {
}
