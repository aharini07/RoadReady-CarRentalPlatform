package com.roadReady.roadready_backend.dto;

import java.time.LocalDateTime;

public record ReviewResponseDto(
        Long reviewId,
        Long reservationId,
        Long carId,
        String carName,
        String customerUsername,
        Integer rating,
        String comment,
        LocalDateTime reviewDate,
        Boolean isDispute,
        String adminReply
) {}