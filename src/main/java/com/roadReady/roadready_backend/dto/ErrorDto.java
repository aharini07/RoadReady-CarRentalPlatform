package com.roadReady.roadready_backend.dto;

import java.time.Instant;

public record ErrorDto(
        String message,
        String description,
        Instant timestamp
) {}
