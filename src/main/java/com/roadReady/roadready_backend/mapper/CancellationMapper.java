package com.roadReady.roadready_backend.mapper;

import org.springframework.stereotype.Component;

import com.roadReady.roadready_backend.dto.CancellationResponseDto;
import com.roadReady.roadready_backend.model.Cancellation;

@Component
public class CancellationMapper {

    public CancellationResponseDto toResponseDto(Cancellation c) {
        return new CancellationResponseDto(
                c.getCancellationId(),
                c.getReservation().getReservationId(),
                c.getCancellationDate(),
                c.getReason(),
                c.getRefundAmount(),
                c.getStatus(),
                c.getReservation().getStatus()
        );
    }
}