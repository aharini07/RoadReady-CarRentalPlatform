package com.roadReady.roadready_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.roadReady.roadready_backend.dto.PaymentRequestDto;
import com.roadReady.roadready_backend.dto.PaymentResponseDto;
import com.roadReady.roadready_backend.service.PaymentService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Pay for a reservation — CUSTOMER only
    @PostMapping("/{reservationId}/pay")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponseDto> pay(
            @PathVariable Long reservationId,
            @Valid @RequestBody PaymentRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.pay(reservationId, dto));
    }

    // View payment status for a reservation — CUSTOMER or ADMIN
    @GetMapping("/{reservationId}/status")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<PaymentResponseDto> status(
            @PathVariable Long reservationId) {

        return ResponseEntity.ok(paymentService.getStatus(reservationId));
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<PaymentResponseDto>> getMyHistory() {
        return ResponseEntity.ok(paymentService.getMyPaymentHistory());
    }
}