package com.roadReady.roadready_backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.roadReady.roadready_backend.dto.RentalAgentDto;
import com.roadReady.roadready_backend.service.RentalAgentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/rental-agents")
public class RentalAgentController {

    private final RentalAgentService rentalAgentService;

    public RentalAgentController(RentalAgentService rentalAgentService) {
        this.rentalAgentService = rentalAgentService;
    }

    // REGISTER RENTAL AGENT — public
    @PostMapping("/register")
    public ResponseEntity<RentalAgentDto> registerRentalAgent(
            @Valid @RequestBody RentalAgentDto dto) {

        RentalAgentDto savedAgent =
                rentalAgentService.registerRentalAgent(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedAgent);
    }

    // GET RENTAL AGENT BY ID
    // SecureConfig allows: RENTAL_AGENT or ADMIN.
    // Ownership check is done inside the service (checkOwner).
    // So no @PreAuthorize here — otherwise RENTAL_AGENT gets 403
    // even when viewing their own profile.
    @GetMapping("/{agentId}")
    public ResponseEntity<RentalAgentDto> getRentalAgentById(
            @PathVariable Long agentId) {

        return ResponseEntity.ok(
                rentalAgentService.getRentalAgentById(agentId));
    }

    // GET ALL RENTAL AGENTS — ADMIN only (SecureConfig enforces)
    @GetMapping("/all")
    public ResponseEntity<List<RentalAgentDto>> getAllRentalAgents() {

        return ResponseEntity.ok(
                rentalAgentService.getAllRentalAgents());
    }

    // UPDATE RENTAL AGENT — ADMIN only
    @PutMapping("/{agentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RentalAgentDto> updateRentalAgent(
            @PathVariable Long agentId,
            @Valid @RequestBody RentalAgentDto dto) {

        return ResponseEntity.ok(
                rentalAgentService.updateRentalAgent(agentId, dto));
    }

    // DELETE RENTAL AGENT — ADMIN only
    @DeleteMapping("/{agentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRentalAgent(
            @PathVariable Long agentId) {

        rentalAgentService.deleteRentalAgent(agentId);
        return ResponseEntity.noContent().build();
    }
}