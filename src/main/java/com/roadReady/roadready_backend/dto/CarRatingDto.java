package com.roadReady.roadready_backend.dto;

public record CarRatingDto(
        Long carId,
        String carName,
        double averageRating,
        long totalReviews
) {}