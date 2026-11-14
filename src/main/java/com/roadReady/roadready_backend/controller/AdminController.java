package com.roadReady.roadready_backend.controller;

import java.util.List;

import com.roadReady.roadready_backend.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.roadReady.roadready_backend.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {

        this.adminService = adminService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<AdminResponseDto>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDto>> getAllUserAccounts() {
        return ResponseEntity.ok(adminService.getAllUserAccounts());
    }

    // VIEW A USER ACCOUNT BY ID
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDto> getUserAccountById(
            @PathVariable Long userId) {
        return ResponseEntity.ok(adminService.getUserAccountById(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<AdminResponseDto> createAdmin(
            @Valid @RequestBody AdminRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminService.createAdmin(dto));
    }
    // GET ADMIN BY ID
    @GetMapping("/{adminId}")
    public ResponseEntity<AdminResponseDto> getAdminById(
            @PathVariable Long adminId) {
        return ResponseEntity.ok(adminService.getAdminById(adminId));
    }

    // UPDATE ADMIN PROFILE
    @PutMapping("/{adminId}")
    public ResponseEntity<AdminResponseDto> updateAdmin(
            @PathVariable Long adminId,
            @Valid @RequestBody AdminUpdateDto dto) {
        return ResponseEntity.ok(adminService.updateAdmin(adminId, dto));
    }

    // DELETE ADMIN PROFILE
    @DeleteMapping("/{adminId}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long adminId) {
        adminService.deleteAdmin(adminId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }
}