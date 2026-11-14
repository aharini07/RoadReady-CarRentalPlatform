package com.roadReady.roadready_backend.service;

import com.roadReady.roadready_backend.model.Reservation;
import com.roadReady.roadready_backend.repository.CancellationRepository;
import com.roadReady.roadready_backend.repository.PaymentRepository;
import com.roadReady.roadready_backend.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CancellationServiceTest {

    @InjectMocks
    private CancellationService cancellationService;

    @Mock private CancellationRepository cancellationRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private com.roadReady.roadready_backend.mapper.CancellationMapper mapper;

    // Since calculateEligibility is private, we test it using Reflection
    private Object invokeCalculateEligibility(Reservation r) throws Exception {
        Method method = CancellationService.class.getDeclaredMethod("calculateEligibility", Reservation.class);
        method.setAccessible(true);
        return method.invoke(cancellationService, r);
    }

    @Test
    public void eligibilityTest_100PercentRefund() throws Exception {
        Reservation r = new Reservation();
        r.setStatus("CONFIRMED");
        r.setTotalAmount(new BigDecimal("1500.00"));
        r.setPickupDate(LocalDate.now().plusDays(5));

        var eligibility = (com.roadReady.roadready_backend.dto.CancellationEligibilityDto) invokeCalculateEligibility(r);

        assertTrue(eligibility.eligible());
        assertEquals(100, eligibility.refundPercentage());
        assertEquals(new BigDecimal("1500.00"), eligibility.refundAmount());
    }

    @Test
    public void eligibilityTest_50PercentRefund() throws Exception {
        Reservation r = new Reservation();
        r.setStatus("CONFIRMED");
        r.setTotalAmount(new BigDecimal("1500.00"));
        r.setPickupDate(LocalDate.now().plusDays(1));

        var eligibility = (com.roadReady.roadready_backend.dto.CancellationEligibilityDto) invokeCalculateEligibility(r);

        assertTrue(eligibility.eligible());
        assertEquals(50, eligibility.refundPercentage());
        assertEquals(new BigDecimal("750.00"), eligibility.refundAmount());
    }

    @Test
    public void eligibilityTest_0PercentRefund() throws Exception {
        Reservation r = new Reservation();
        r.setStatus("CONFIRMED");
        r.setTotalAmount(new BigDecimal("1500.00"));
        r.setPickupDate(LocalDate.now());

        var eligibility = (com.roadReady.roadready_backend.dto.CancellationEligibilityDto) invokeCalculateEligibility(r);

        assertTrue(eligibility.eligible());
        assertEquals(0, eligibility.refundPercentage());
        assertEquals(new BigDecimal("0.00"), eligibility.refundAmount());
    }

    @Test
    public void eligibilityTest_AlreadyCancelled() throws Exception {
        Reservation r = new Reservation();
        r.setStatus("CANCELLED");

        var eligibility = (com.roadReady.roadready_backend.dto.CancellationEligibilityDto) invokeCalculateEligibility(r);

        assertFalse(eligibility.eligible());
        assertEquals("Already cancelled.", eligibility.policyReason());
    }
}