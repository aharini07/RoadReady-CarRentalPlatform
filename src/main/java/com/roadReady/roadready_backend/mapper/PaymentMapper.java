package com.roadReady.roadready_backend.mapper;

import org.springframework.stereotype.Component;

import com.roadReady.roadready_backend.dto.PaymentResponseDto;
import com.roadReady.roadready_backend.model.Payment;

@Component
public class PaymentMapper {

    public PaymentResponseDto toResponseDto(Payment p) {
        return new PaymentResponseDto(
                p.getPaymentId(),
                p.getReservation().getReservationId(),
                p.getAmount(),
                p.getPaymentMethod(),
                p.getPaymentDate(),
                p.getPaymentStatus(),
                p.getTransactionId()
        );
    }
}