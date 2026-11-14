package com.roadReady.roadready_backend.mapper;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.roadReady.roadready_backend.dto.ReservationResponseDto;
import com.roadReady.roadready_backend.model.Reservation;

@Component
public class ReservationMapper {

    public ReservationResponseDto toResponseDto(Reservation r) {
        long days = ChronoUnit.DAYS.between(
                r.getPickupDate(), r.getDropoffDate());
        if (days <= 0) days = 1;   // same-day = 1 rental day

        return new ReservationResponseDto(
                r.getReservationId(),
                r.getCustomer().getCustomerId(),
                r.getCustomer().getUsers().getUsername(),
                r.getCar().getCarId(),
                r.getCar().getCarName(),
                r.getCar().getBrand(),
                r.getPickupLocation(),    // <--- NEW
                r.getDropoffLocation(),
                r.getPickupDate(),
                r.getPickupTime(),
                r.getDropoffDate(),
                r.getNumPassengers(),
                r.getTotalAmount() == null ? BigDecimal.ZERO : r.getTotalAmount(),
                r.getPenaltyFee() == null ? BigDecimal.ZERO : r.getPenaltyFee(),
                r.getStatus(),
                r.getCreatedAt(),
                days
        );
    }
}