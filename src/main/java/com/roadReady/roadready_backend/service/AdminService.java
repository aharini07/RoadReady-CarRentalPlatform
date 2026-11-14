package com.roadReady.roadready_backend.service;

import java.math.BigDecimal;
import java.util.List;

import com.roadReady.roadready_backend.dto.*;
import org.springframework.stereotype.Service;
import com.roadReady.roadready_backend.exception.AdminNotFoundException;
import com.roadReady.roadready_backend.mapper.AdminMapper;
import com.roadReady.roadready_backend.model.Admin;
import com.roadReady.roadready_backend.model.Users;
import com.roadReady.roadready_backend.repository.AdminRepository;
import com.roadReady.roadready_backend.repository.CarRepository;
import com.roadReady.roadready_backend.repository.PaymentRepository;
import com.roadReady.roadready_backend.repository.ReservationRepository;
import com.roadReady.roadready_backend.repository.UserRepository;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final AdminMapper adminMapper;

    private final CarRepository carRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    public AdminService(
            AdminRepository adminRepository,
            UserRepository userRepository,
            AdminMapper adminMapper,
            CarRepository carRepository,
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository) {

        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.adminMapper = adminMapper;
        this.carRepository = carRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
    }

    public AdminResponseDto createAdmin(AdminRequestDto dto) {
        Users user = userRepository.findById(dto.userId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found with ID: " + dto.userId()));

        if (!"ADMIN".equalsIgnoreCase(user.getRole().name())) {
            throw new IllegalArgumentException(
                    "The user must be admin");
        }

        if (adminRepository.existsByUsers_UserId(dto.userId())) {
            throw new IllegalArgumentException(
                    "An admin profile already exists for this user");
        }

        Admin admin = adminMapper.toEntity(dto, user);
        Admin savedAdmin = adminRepository.save(admin);

        return adminMapper.toResponseDto(savedAdmin);
    }

    public AdminResponseDto getAdminById(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() ->
                        new AdminNotFoundException(
                                "Admin not found with ID: " + adminId));

        return adminMapper.toResponseDto(admin);
    }


    public List<AdminResponseDto> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(adminMapper::toResponseDto)
                .toList();
    }

    public AdminResponseDto updateAdmin(
            Long adminId,
            AdminUpdateDto dto) {

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() ->
                        new AdminNotFoundException(
                                "Admin not found with ID: " + adminId));

        adminMapper.updateEntity(admin, dto);
        Admin updatedAdmin = adminRepository.save(admin);

        return adminMapper.toResponseDto(updatedAdmin);
    }

    public void deleteAdmin(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() ->
                        new AdminNotFoundException(
                                "Admin not found with ID: " + adminId));

        adminRepository.delete(admin);
    }

    public List<UserResponseDto> getAllUserAccounts() {
        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponseDto(
                        user.getUserId(),
                        user.getUsername(),
                        user.getRole()
                ))
                .toList();
    }

    public UserResponseDto getUserAccountById(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new com.roadReady.roadready_backend.exception
                        .UserNotFoundException(
                        "User not found with ID: " + userId));

        return new UserResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getRole()
        );
    }

    // ADMIN DASHBOARD STATS
    public DashboardStatsDto getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalCars = carRepository.count();
        long totalReservations = reservationRepository.count();

        long activeBookings = reservationRepository.findAll().stream()
                .filter(r -> "CONFIRMED".equalsIgnoreCase(r.getStatus())
                        || "PICKED_UP".equalsIgnoreCase(r.getStatus()))
                .count();

        long cancelledBookings = reservationRepository.findAll().stream()
                .filter(r -> "CANCELLED".equalsIgnoreCase(r.getStatus()))
                .count();

        // Revenue = Only count payments that were successful (not refunded)
        BigDecimal totalRevenue = paymentRepository.findAll().stream()
                .filter(p -> "SUCCESS".equalsIgnoreCase(p.getPaymentStatus()))
                .map(p -> p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardStatsDto(totalUsers, totalCars, totalReservations, activeBookings, cancelledBookings, totalRevenue);
    }
}