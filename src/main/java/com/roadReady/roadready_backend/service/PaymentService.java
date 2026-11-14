package com.roadReady.roadready_backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roadReady.roadready_backend.dto.PaymentRequestDto;
import com.roadReady.roadready_backend.dto.PaymentResponseDto;
import com.roadReady.roadready_backend.exception.PaymentNotFoundException;
import com.roadReady.roadready_backend.exception.ReservationNotFoundException;
import com.roadReady.roadready_backend.mapper.PaymentMapper;
import com.roadReady.roadready_backend.model.Payment;
import com.roadReady.roadready_backend.model.Reservation;
import com.roadReady.roadready_backend.repository.PaymentRepository;
import com.roadReady.roadready_backend.repository.ReservationRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentMapper paymentMapper;

    public PaymentService(PaymentRepository paymentRepository,
                          ReservationRepository reservationRepository,
                          PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
        this.paymentMapper = paymentMapper;
    }

    @Transactional
    public PaymentResponseDto pay(Long reservationId, PaymentRequestDto dto) {

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found with ID: " + reservationId));

        requireOwnerOrAdmin(r);

        // Check if a payment already exists
        boolean hasExistingPayment = paymentRepository.existsByReservation_ReservationId(reservationId);

        BigDecimal amountToCharge;

        if (hasExistingPayment) {
            // If they already paid, they can ONLY pay again if there is an unpaid penalty fee
            if (r.getPenaltyFee() == null || r.getPenaltyFee().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException("This reservation has already been paid for, and there is no penalty due.");
            }
            // Charge only the penalty amount
            amountToCharge = r.getPenaltyFee();

            // Reset penalty fee so they can't pay it twice
            r.setPenaltyFee(BigDecimal.ZERO);

        } else {
            // First time payment: Only PENDING reservations can be paid
            if (!"PENDING".equalsIgnoreCase(r.getStatus())) {
                throw new IllegalStateException(
                        "Reservation status is " + r.getStatus()
                                + "; only PENDING reservations can be paid initially.");
            }

            if (r.getTotalAmount() == null) {
                throw new IllegalStateException("Reservation has no total amount; cannot process payment");
            }

            // Charge the full amount
            amountToCharge = r.getTotalAmount();

            // Change status to CONFIRMED
            r.setStatus("CONFIRMED");
        }

        // Simulate gateway success for the new payment record
        Payment p = new Payment();
        p.setReservation(r);
        p.setAmount(amountToCharge);
        p.setPaymentMethod(dto.paymentMethod().toUpperCase());
        p.setPaymentDate(LocalDateTime.now());
        p.setPaymentStatus("SUCCESS");
        p.setTransactionId("TXN-" +
                UUID.randomUUID().toString().replace("-", "").substring(0, 12)
                        .toUpperCase());

        Payment saved = paymentRepository.save(p);
        reservationRepository.save(r); // Save the updated reservation (clears penalty or updates status)

        return paymentMapper.toResponseDto(saved);
    }


    public PaymentResponseDto getStatus(Long reservationId) {

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found with ID: " + reservationId));

        requireOwnerOrAdmin(r);

        Payment p = paymentRepository.findByReservation_ReservationId(reservationId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "No payment recorded for reservation " + reservationId));

        return paymentMapper.toResponseDto(p);
    }


    private void requireOwnerOrAdmin(Reservation r) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || "anonymousUser".equals(auth.getName())) {
            throw new AccessDeniedException("Authentication required");
        }

        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean owner = r.getCustomer() != null
                && r.getCustomer().getUsers() != null
                && auth.getName().equals(r.getCustomer().getUsers().getUsername());

        if (!admin && !owner) {
            throw new AccessDeniedException(
                    "You can only pay for, or view payments of, your own reservations");
        }
    }
    public List<PaymentResponseDto> getMyPaymentHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return paymentRepository.findByReservation_Customer_Users_Username(username)
                .stream()
                .map(paymentMapper::toResponseDto)
                .toList();
    }
}