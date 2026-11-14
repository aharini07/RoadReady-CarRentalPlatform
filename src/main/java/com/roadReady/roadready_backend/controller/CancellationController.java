package com.roadReady.roadready_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.roadReady.roadready_backend.dto.CancellationEligibilityDto;
import com.roadReady.roadready_backend.dto.CancellationRequestDto;
import com.roadReady.roadready_backend.dto.CancellationResponseDto;
import com.roadReady.roadready_backend.service.CancellationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class CancellationController {

    private final CancellationService cancellationService;

    public CancellationController(CancellationService cancellationService) {
        this.cancellationService = cancellationService;
    }

    // Check refund amount before cancelling
    @GetMapping("/cancellations/{reservationId}/eligibility")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<CancellationEligibilityDto> checkEligibility(
            @PathVariable Long reservationId) {

        return ResponseEntity.ok(cancellationService.checkEligibility(reservationId));
    }

    // Execute the cancellation
    @PostMapping("/reservations/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<CancellationResponseDto> cancel(
            @PathVariable Long id,
            @Valid @RequestBody CancellationRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cancellationService.cancel(id, dto));
    }
}