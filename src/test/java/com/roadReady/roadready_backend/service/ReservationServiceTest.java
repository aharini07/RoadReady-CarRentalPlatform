package com.roadReady.roadready_backend.service;

import com.roadReady.roadready_backend.dto.ReservationRequestDto;
import com.roadReady.roadready_backend.mapper.ReservationMapper;
import com.roadReady.roadready_backend.model.Car;
import com.roadReady.roadready_backend.model.Customer;
import com.roadReady.roadready_backend.model.Users;
import com.roadReady.roadready_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock private ReservationRepository reservationRepository;
    @Mock private CarRepository carRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReservationMapper reservationMapper;

    private void mockSecurityContext() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
    }

    // ---- book : overlap rejected ----
    @Test
    public void bookTest_OverlapRejected() {
        mockSecurityContext();

        ReservationRequestDto dto = new ReservationRequestDto(
                1L, "Airport", "T Nagar",
                LocalDate.of(2026, 11, 10), LocalTime.of(10, 0),
                LocalDate.of(2026, 11, 13), 2
        );

        Car car = new Car();
        car.setCarId(1L);
        car.setStatus("AVAILABLE");
        car.setDailyRate(new BigDecimal("1500"));
        car.setSeatingCapacity(5);

        Users user = new Users();
        user.setUserId(1L);
        user.setUsername("testuser");

        Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setUsers(user);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(reservationRepository.countOverlapping(anyLong(), any(), any())).thenReturn(1L);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reservationService.book(dto);
        });

        assertEquals("Car is already booked for the requested dates", exception.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    // ---- book : car not available ----
    @Test
    public void bookTest_CarNotAvailable() {
        mockSecurityContext();

        ReservationRequestDto dto = new ReservationRequestDto(
                1L, "Airport", "T Nagar",
                LocalDate.now().plusDays(1), LocalTime.of(10, 0),
                LocalDate.now().plusDays(3), 2
        );

        Car car = new Car();
        car.setCarId(1L);
        car.setStatus("MAINTENANCE");

        Users user = new Users();
        user.setUserId(1L);
        user.setUsername("testuser");
        Customer customer = new Customer();
        customer.setUsers(user);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        assertThrows(IllegalStateException.class, () -> reservationService.book(dto));
        verify(reservationRepository, never()).save(any());
    }
}