package com.roadReady.roadready_backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roadReady.roadready_backend.dto.ReservationRequestDto;
import com.roadReady.roadready_backend.dto.ReservationResponseDto;
import com.roadReady.roadready_backend.exception.ReservationNotFoundException;
import com.roadReady.roadready_backend.mapper.ReservationMapper;
import com.roadReady.roadready_backend.model.Car;
import com.roadReady.roadready_backend.model.Customer;
import com.roadReady.roadready_backend.model.Reservation;
import com.roadReady.roadready_backend.repository.CarRepository;
import com.roadReady.roadready_backend.repository.CustomerRepository;
import com.roadReady.roadready_backend.repository.ReservationRepository;
import com.roadReady.roadready_backend.repository.UserRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;

    public ReservationService(
            ReservationRepository reservationRepository,
            CarRepository carRepository,
            CustomerRepository customerRepository,
            UserRepository userRepository,
            ReservationMapper reservationMapper) {

        this.reservationRepository = reservationRepository;
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.reservationMapper = reservationMapper;
    }

    @Transactional
    public ReservationResponseDto book(ReservationRequestDto dto) {

        Customer customer = currentCustomer();

        if (dto.dropoffDate().isBefore(dto.pickupDate())) {
            throw new IllegalArgumentException("dropoffDate cannot be before pickupDate");
        }

        Car car = carRepository.findById(dto.carId())
                .orElseThrow(() -> new IllegalArgumentException("Car not found with ID: " + dto.carId()));

        if (car.getStatus() == null || !"AVAILABLE".equalsIgnoreCase(car.getStatus())) {
            throw new IllegalStateException("Car is not available for booking (current status: " + car.getStatus() + ")");
        }

        if (dto.numPassengers() != null && car.getSeatingCapacity() != null
                && dto.numPassengers() > car.getSeatingCapacity()) {
            throw new IllegalArgumentException("Requested passengers (" + dto.numPassengers()
                    + ") exceed car capacity (" + car.getSeatingCapacity() + ")");
        }

        long overlapping = reservationRepository.countOverlapping(
                car.getCarId(), dto.pickupDate(), dto.dropoffDate());

        if (overlapping > 0) {
            throw new IllegalStateException("Car is already booked for the requested dates");
        }

        long days = ChronoUnit.DAYS.between(dto.pickupDate(), dto.dropoffDate());
        if (days <= 0) days = 1;

        BigDecimal subtotal = car.getDailyRate().multiply(BigDecimal.valueOf(days));
        BigDecimal taxRate = new BigDecimal("0.05");
        BigDecimal tax = subtotal.multiply(taxRate);
        BigDecimal totalAmount = subtotal.add(tax);

        Reservation r = new Reservation();
        r.setCustomer(customer);
        r.setCar(car);
        r.setPickupLocation(dto.pickupLocation());
        r.setDropoffLocation(dto.dropoffLocation());
        r.setPickupDate(dto.pickupDate());
        r.setPickupTime(dto.pickupTime());
        r.setDropoffDate(dto.dropoffDate());
        r.setNumPassengers(dto.numPassengers());
        r.setTotalAmount(totalAmount);
        r.setStatus("PENDING");
        r.setCreatedAt(LocalDateTime.now());

        return reservationMapper.toResponseDto(reservationRepository.save(r));
    }

    public List<ReservationResponseDto> myBookings() {
        Customer customer = currentCustomer();
        return reservationRepository
                .findByCustomer_CustomerIdOrderByCreatedAtDesc(customer.getCustomerId())
                .stream()
                .map(reservationMapper::toResponseDto)
                .toList();
    }


    public ReservationResponseDto getDetails(Long reservationId) {

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found with ID: " + reservationId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || "anonymousUser".equals(auth.getName())) {
            throw new AccessDeniedException("Authentication required");
        }

        // 1. Allow Admins and Rental Agents
        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_RENTAL_AGENT"));

        // 2. Allow the Customer who owns the booking
        boolean owner = r.getCustomer().getUsers().getUsername().equals(auth.getName());

        if (!isStaff && !owner) {
            throw new AccessDeniedException("You can view only your own reservations");
        }

        return reservationMapper.toResponseDto(r);
    }


    private Customer currentCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || "anonymousUser".equals(auth.getName())) {
            throw new AccessDeniedException("Authentication required");
        }

        String username = auth.getName();

        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"))
                .getUserId();

        return customerRepository.findAll().stream()
                .filter(c -> c.getUsers() != null
                        && userId.equals(c.getUsers().getUserId()))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("You do not have a customer profile"));
    }
    // ADMIN ONLY: View all bookings in the system
    public List<ReservationResponseDto> getAllReservationsForAdmin() {
        return reservationRepository.findAll()
                .stream()
                .map(reservationMapper::toResponseDto)
                .sorted((a, b) -> b.createdAt().compareTo(a.createdAt())) // Newest first
                .toList();
    }
}