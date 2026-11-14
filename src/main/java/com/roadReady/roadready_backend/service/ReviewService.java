package com.roadReady.roadready_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roadReady.roadready_backend.dto.CarRatingDto;
import com.roadReady.roadready_backend.dto.ReviewRequestDto;
import com.roadReady.roadready_backend.dto.ReviewResponseDto;
import com.roadReady.roadready_backend.exception.ReservationNotFoundException;
import com.roadReady.roadready_backend.mapper.ReviewMapper;
import com.roadReady.roadready_backend.model.Reservation;
import com.roadReady.roadready_backend.model.Review;
import com.roadReady.roadready_backend.repository.CarRepository;
import com.roadReady.roadready_backend.repository.ReservationRepository;
import com.roadReady.roadready_backend.repository.ReviewRepository;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final CarRepository carRepository;
    private final ReviewMapper mapper;

    public ReviewService(ReviewRepository reviewRepository,
                         ReservationRepository reservationRepository,
                         CarRepository carRepository,
                         ReviewMapper mapper) {
        this.reviewRepository = reviewRepository;
        this.reservationRepository = reservationRepository;
        this.carRepository = carRepository;
        this.mapper = mapper;
    }

    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto dto) {

        Reservation r = reservationRepository.findById(dto.reservationId())
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found with ID: " + dto.reservationId()));

        // 1. Ownership check
        requireOwner(r);

        // 2. Guard: status must be COMPLETED
        if (!"COMPLETED".equalsIgnoreCase(r.getStatus())) {
            throw new IllegalStateException(
                    "Cannot review. Reservation status is " + r.getStatus()
                            + "; only COMPLETED rentals can be reviewed.");
        }

        // 3. no duplicate review
        if (reviewRepository.existsByReservation_ReservationId(r.getReservationId())) {
            throw new IllegalStateException(
                    "A review already exists for this reservation.");
        }

        // 4. Create and save
        Review review = new Review();
        review.setReservation(r);
        review.setRating(dto.rating());
        review.setComment(dto.comment());
        review.setReviewDate(LocalDateTime.now());

        // 5. Save the dispute flag (Default to false if null)
        review.setIsDispute(dto.isDispute() != null ? dto.isDispute() : false);

        return mapper.toResponseDto(reviewRepository.save(review));
    }

    // ADMIN METHOD: Reply to a dispute
    @Transactional
    public ReviewResponseDto replyToDispute(Long reviewId, String reply) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with ID: " + reviewId));

        review.setAdminReply(reply);
        Review savedReview = reviewRepository.save(review);

        return mapper.toResponseDto(savedReview);
    }

    public List<ReviewResponseDto> getReviewsForCar(Long carId) {

        if (!carRepository.existsById(carId)) {
            throw new com.roadReady.roadready_backend.exception.CarNotFoundException(
                    "Car not found with ID: " + carId);
        }

        return reviewRepository.findByReservation_Car_CarIdOrderByReviewDateDesc(carId)
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    public CarRatingDto getCarRating(Long carId) {

        var car = carRepository.findById(carId)
                .orElseThrow(() -> new com.roadReady.roadready_backend.exception.CarNotFoundException(
                        "Car not found with ID: " + carId));

        Double avg = reviewRepository.findAverageRatingByCarId(carId);
        long count = reviewRepository.countByReservation_Car_CarId(carId);

        double rounded = avg == null ? 0.0
                : Math.round(avg * 10.0) / 10.0;

        return new CarRatingDto(car.getCarId(), car.getCarName(), rounded, count);
    }

    private void requireOwner(Reservation r) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || "anonymousUser".equals(auth.getName())) {
            throw new AccessDeniedException("Authentication required");
        }

        boolean owner = r.getCustomer() != null
                && r.getCustomer().getUsers() != null
                && auth.getName().equals(r.getCustomer().getUsers().getUsername());

        if (!owner) {
            throw new AccessDeniedException(
                    "You can only review your own rentals");
        }
    }
}