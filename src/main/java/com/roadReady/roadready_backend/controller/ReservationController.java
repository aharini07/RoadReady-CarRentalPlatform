package com.roadReady.roadready_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.roadReady.roadready_backend.dto.ReservationRequestDto;
import com.roadReady.roadready_backend.dto.ReservationResponseDto;
import com.roadReady.roadready_backend.service.ReservationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // Book a car — CUSTOMER only
    @PostMapping("/book")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReservationResponseDto> book(
            @Valid @RequestBody ReservationRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.book(dto));
    }

    // List own bookings — CUSTOMER only
    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ReservationResponseDto>> myBookings() {
        return ResponseEntity.ok(reservationService.myBookings());
    }

    // View a single reservation — owner, ADMIN, or RENTAL_AGENT
    // (service layer enforces ownership for customers)
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'RENTAL_AGENT')") // <--- UPDATED HERE
    public ResponseEntity<ReservationResponseDto> getDetails(
            @PathVariable Long id) {

        return ResponseEntity.ok(reservationService.getDetails(id));
    }
    // ADMIN: View all reservations
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationResponseDto>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservationsForAdmin());
    }
}