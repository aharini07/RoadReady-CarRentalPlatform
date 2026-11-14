package com.roadReady.roadready_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.roadReady.roadready_backend.dto.CheckInOutResponseDto;
import com.roadReady.roadready_backend.dto.InspectionReportDto;
import com.roadReady.roadready_backend.dto.PickupRequestDto;
import com.roadReady.roadready_backend.dto.ReturnRequestDto;
import com.roadReady.roadready_backend.service.CheckInOutService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/check-in-out")
public class CheckInOutController {

    private final CheckInOutService service;

    public CheckInOutController(CheckInOutService service) {
        this.service = service;
    }

    @PostMapping("/{reservationId}/pickup")
    @PreAuthorize("hasRole('RENTAL_AGENT')")
    public ResponseEntity<CheckInOutResponseDto> pickup(
            @PathVariable Long reservationId,
            @Valid @RequestBody PickupRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.recordPickup(reservationId, dto));
    }

    @PostMapping("/{reservationId}/return")
    @PreAuthorize("hasRole('RENTAL_AGENT')")
    public ResponseEntity<CheckInOutResponseDto> returnCar(
            @PathVariable Long reservationId,
            @Valid @RequestBody ReturnRequestDto dto) {

        return ResponseEntity.ok(service.recordReturn(reservationId, dto));
    }

    @GetMapping("/{reservationId}/report")
    @PreAuthorize("hasAnyRole('RENTAL_AGENT', 'ADMIN')")
    public ResponseEntity<InspectionReportDto> report(
            @PathVariable Long reservationId) {

        return ResponseEntity.ok(service.getReport(reservationId));
    }
}