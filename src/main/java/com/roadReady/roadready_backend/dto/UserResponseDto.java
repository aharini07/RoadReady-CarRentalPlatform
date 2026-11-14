package com.roadReady.roadready_backend.dto;

import com.roadReady.roadready_backend.enums.Role;

public record UserResponseDto(
        Long userId,
        String username,
        Role role
) {
}