package com.roadReady.roadready_backend.service;

import com.roadReady.roadready_backend.dto.PaymentRequestDto;
import com.roadReady.roadready_backend.dto.PaymentResponseDto;
import com.roadReady.roadready_backend.model.Payment;
import com.roadReady.roadready_backend.model.Reservation;
import com.roadReady.roadready_backend.model.Users;
import com.roadReady.roadready_backend.model.Customer;
import com.roadReady.roadready_backend.mapper.PaymentMapper;
import com.roadReady.roadready_backend.repository.PaymentRepository;
import com.roadReady.roadready_backend.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock private PaymentRepository paymentRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private PaymentMapper paymentMapper;

    private void mockSecurityContext() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        lenient().when(auth.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void payTest_Success() {
        mockSecurityContext();

        Users user = new Users();
        user.setUsername("testuser");
        Customer customer = new Customer();
        customer.setUsers(user);

        Reservation r = new Reservation();
        r.setReservationId(1L);
        r.setStatus("PENDING");
        r.setTotalAmount(new BigDecimal("4725.00"));
        r.setCustomer(customer);

        PaymentRequestDto dto = new PaymentRequestDto("CARD");

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(paymentRepository.existsByReservation_ReservationId(1L)).thenReturn(false);

        Payment savedPayment = new Payment();
        savedPayment.setPaymentId(1L);
        savedPayment.setAmount(r.getTotalAmount());
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // FIXED: Mock the mapper to return a valid DTO
        PaymentResponseDto mockResponse = new PaymentResponseDto(
                1L, 1L, new BigDecimal("4725.00"), "CARD", LocalDateTime.now(), "SUCCESS", "TXN-123"
        );
        when(paymentMapper.toResponseDto(any(Payment.class))).thenReturn(mockResponse);

        PaymentResponseDto response = paymentService.pay(1L, dto);

        assertNotNull(response);
        assertEquals(new BigDecimal("4725.00"), response.amount());
        assertEquals("CONFIRMED", r.getStatus());

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(reservationRepository, times(1)).save(r);
    }

    @Test
    public void payTest_DuplicateRejected() {
        mockSecurityContext();

        Users user = new Users();
        user.setUsername("testuser");
        Customer customer = new Customer();
        customer.setUsers(user);

        Reservation r = new Reservation();
        r.setReservationId(1L);
        r.setStatus("CONFIRMED");
        r.setCustomer(customer);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));
        when(paymentRepository.existsByReservation_ReservationId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> paymentService.pay(1L, new PaymentRequestDto("CARD")));
        verify(paymentRepository, never()).save(any());
    }
}