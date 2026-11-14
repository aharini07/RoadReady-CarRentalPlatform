
package com.roadReady.roadready_backend.dto;

public record AdminResponseDto(
        Long adminId,
        Long userId,
        String username,
        String role,
        String name,
        String phoneNo,
        String department
) {}
