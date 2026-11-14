package com.roadReady.roadready_backend.dto;

public record ProfileUpdateDto(
        String name,
        String phoneNo,
        String address,
        String city,
        String country
) {
}
