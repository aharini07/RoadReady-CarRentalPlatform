package com.roadReady.roadready_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.roadReady.roadready_backend.dto.PricingRequestDto;
import com.roadReady.roadready_backend.dto.PricingResponseDto;
import com.roadReady.roadready_backend.service.PricingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    // Public — estimate a rental price without touching reservations
    @PostMapping("/estimate")
    public ResponseEntity<PricingResponseDto> estimate(
            @Valid @RequestBody PricingRequestDto dto) {
        return ResponseEntity.ok(pricingService.estimate(dto));
    }
}