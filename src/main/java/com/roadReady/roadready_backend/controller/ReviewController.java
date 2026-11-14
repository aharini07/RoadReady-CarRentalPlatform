package com.roadReady.roadready_backend.controller;

import java.util.List;

import com.roadReady.roadready_backend.dto.AdminReplyRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.roadReady.roadready_backend.dto.CarRatingDto;
import com.roadReady.roadready_backend.dto.ReviewRequestDto;
import com.roadReady.roadready_backend.dto.ReviewResponseDto;
import com.roadReady.roadready_backend.service.ReviewService;

import jakarta.validation.Valid;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Post a review — CUSTOMER only
    @PostMapping("/api/v1/reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponseDto> createReview(
            @Valid @RequestBody ReviewRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(dto));
    }

    // Get reviews for a car — PUBLIC
    @GetMapping("/api/v1/cars/{carId}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviews(
            @PathVariable Long carId) {

        return ResponseEntity.ok(reviewService.getReviewsForCar(carId));
    }

    // Get average rating — PUBLIC
    @GetMapping("/api/v1/cars/{carId}/rating")
    public ResponseEntity<CarRatingDto> getRating(
            @PathVariable Long carId) {

        return ResponseEntity.ok(reviewService.getCarRating(carId));
    }
    // ADMIN: Reply to a customer dispute
    @PostMapping("/api/v1/reviews/{reviewId}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponseDto> replyToDispute(
            @PathVariable Long reviewId,
            @Valid @RequestBody AdminReplyRequestDto dto) {

        return ResponseEntity.ok(reviewService.replyToDispute(reviewId, dto.reply()));
    }
}