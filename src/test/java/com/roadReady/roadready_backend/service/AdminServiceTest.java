package com.roadReady.roadready_backend.service;

import com.roadReady.roadready_backend.dto.AdminRequestDto;
import com.roadReady.roadready_backend.dto.AdminResponseDto;
import com.roadReady.roadready_backend.dto.DashboardStatsDto;
import com.roadReady.roadready_backend.enums.Role;
import com.roadReady.roadready_backend.mapper.AdminMapper;
import com.roadReady.roadready_backend.model.Admin;
import com.roadReady.roadready_backend.model.Payment;
import com.roadReady.roadready_backend.model.Reservation;
import com.roadReady.roadready_backend.model.Users;
import com.roadReady.roadready_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock private AdminRepository adminRepository;
    @Mock private UserRepository userRepository;
    @Mock private AdminMapper adminMapper;
    @Mock private CarRepository carRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private PaymentRepository paymentRepository;

    @Test
    public void createAdminTest_Success() {
        AdminRequestDto dto = new AdminRequestDto(1L, "Super Admin", "9876543210", "Management");

        Users user = new Users();
        user.setUserId(1L);
        user.setRole(Role.ADMIN);

        Admin mockAdmin = new Admin();
        mockAdmin.setAdminId(1L);
        mockAdmin.setName("Super Admin");

        AdminResponseDto expectedResponse = new AdminResponseDto(
                1L, 1L, "admin", "ADMIN", "Super Admin", "9876543210", "Management"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(adminRepository.existsByUsers_UserId(1L)).thenReturn(false);
        when(adminMapper.toEntity(dto, user)).thenReturn(mockAdmin);
        when(adminRepository.save(mockAdmin)).thenReturn(mockAdmin);
        when(adminMapper.toResponseDto(mockAdmin)).thenReturn(expectedResponse);

        AdminResponseDto result = adminService.createAdmin(dto);

        assertEquals("Super Admin", result.name());
        verify(adminRepository, times(1)).save(mockAdmin);
    }

    @Test
    public void createAdminTest_NotAdminRole() {
        AdminRequestDto dto = new AdminRequestDto(1L, "Fake Admin", "9876543210", "IT");

        Users user = new Users();
        user.setUserId(1L);
        user.setRole(Role.CUSTOMER); // Not ADMIN

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminService.createAdmin(dto);
        });

        assertEquals("The user must be admin", exception.getMessage());
        verify(adminRepository, never()).save(any());
    }

    @Test
    public void createAdminTest_Duplicate() {
        AdminRequestDto dto = new AdminRequestDto(1L, "Super Admin", "9876543210", "Management");

        Users user = new Users();
        user.setUserId(1L);
        user.setRole(Role.ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(adminRepository.existsByUsers_UserId(1L)).thenReturn(true); // Already exists

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            adminService.createAdmin(dto);
        });

        assertEquals("An admin profile already exists for this user", exception.getMessage());
        verify(adminRepository, never()).save(any());
    }


    @Test
    public void getDashboardStatsTest_CorrectMath() {
        // Setup total counts
        when(userRepository.count()).thenReturn(10L);
        when(carRepository.count()).thenReturn(5L);
        when(reservationRepository.count()).thenReturn(20L);

        // Setup Reservations for active/cancelled count
        Reservation active = new Reservation();
        active.setStatus("CONFIRMED");
        Reservation pickedUp = new Reservation();
        pickedUp.setStatus("PICKED_UP");
        Reservation cancelled = new Reservation();
        cancelled.setStatus("CANCELLED");

        when(reservationRepository.findAll()).thenReturn(List.of(active, pickedUp, cancelled));

        // Setup Payments for revenue calculation
        Payment success1 = new Payment();
        success1.setPaymentStatus("SUCCESS");
        success1.setAmount(new BigDecimal("1500.00"));

        Payment success2 = new Payment();
        success2.setPaymentStatus("SUCCESS");
        success2.setAmount(new BigDecimal("2500.00"));

        Payment refunded = new Payment();
        refunded.setPaymentStatus("REFUNDED"); // Should NOT be counted
        refunded.setAmount(new BigDecimal("1000.00"));

        when(paymentRepository.findAll()).thenReturn(List.of(success1, success2, refunded));

        // Act
        DashboardStatsDto stats = adminService.getDashboardStats();

        // Assert
        assertEquals(10L, stats.totalUsers());
        assertEquals(5L, stats.totalCars());
        assertEquals(20L, stats.totalReservations());

        // 2 Active bookings (CONFIRMED + PICKED_UP)
        assertEquals(2L, stats.activeBookings());

        // 1 Cancelled
        assertEquals(1L, stats.cancelledBookings());

        // Revenue should only be 1500 + 2500 = 4000 (Refunded 1000 is ignored)
        assertEquals(new BigDecimal("4000.00"), stats.totalRevenue());
    }
}