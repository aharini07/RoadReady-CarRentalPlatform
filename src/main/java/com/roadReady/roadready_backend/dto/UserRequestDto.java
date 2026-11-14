package com.roadReady.roadready_backend.dto;

import com.roadReady.roadready_backend.enums.Role;

public record UserRequestDto(
        String username,
        String password,
        Role role,

        // NEW FIELDS
        String name,
        String phoneNo,
        String address,
        String city,
        String country
) {
}