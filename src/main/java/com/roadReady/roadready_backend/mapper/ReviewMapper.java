package com.roadReady.roadready_backend.mapper;

import org.springframework.stereotype.Component;

import com.roadReady.roadready_backend.dto.ReviewResponseDto;
import com.roadReady.roadready_backend.model.Review;

@Component
public class ReviewMapper {

    public ReviewResponseDto toResponseDto(Review r) {
        return new ReviewResponseDto(
                r.getReviewId(),
                r.getReservation().getReservationId(),
                r.getReservation().getCar().getCarId(),
                r.getReservation().getCar().getCarName(),
                r.getReservation().getCustomer().getUsers().getUsername(),
                r.getRating(),
                r.getComment(),
                r.getReviewDate(),
                r.getIsDispute(),   // <--- NEW
                r.getAdminReply()
        );
    }
}