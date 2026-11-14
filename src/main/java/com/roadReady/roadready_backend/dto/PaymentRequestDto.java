package com.roadReady.roadready_backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequestDto(
        @NotBlank(message = "Payment method is required")
        String paymentMethod   // "CARD" | "UPI" | "NETBANKING" | "WALLET"
) {}