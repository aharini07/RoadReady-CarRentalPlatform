package com.roadReady.roadready_backend.dto;

import java.math.BigDecimal;

public record DashboardStatsDto(
        long totalUsers,
        long totalCars,
        long totalReservations,
        long activeBookings,
        long cancelledBookings,
        BigDecimal totalRevenue
) {}
