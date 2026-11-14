package com.roadReady.roadready_backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminReplyRequestDto(
        @NotBlank(message = "Reply message cannot be empty")
        String reply
) {

}
