package com.roadReady.roadready_backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roadReady.roadready_backend.dto.CancellationEligibilityDto;
import com.roadReady.roadready_backend.dto.CancellationRequestDto;
import com.roadReady.roadready_backend.dto.CancellationResponseDto;
import com.roadReady.roadready_backend.exception.ReservationNotFoundException;
import com.roadReady.roadready_backend.mapper.CancellationMapper;
import com.roadReady.roadready_backend.model.Cancellation;
import com.roadReady.roadready_backend.model.Payment;
import com.roadReady.roadready_backend.model.Reservation;
import com.roadReady.roadready_backend.repository.CancellationRepository;
import com.roadReady.roadready_backend.repository.PaymentRepository;
import com.roadReady.roadready_backend.repository.ReservationRepository;

@Service
public class CancellationService {

    private final CancellationRepository cancellationRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final CancellationMapper mapper;

    public CancellationService(CancellationRepository cancellationRepository,
                               ReservationRepository reservationRepository,
                               PaymentRepository paymentRepository,
                               CancellationMapper mapper) {
        this.cancellationRepository = cancellationRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.mapper = mapper;
    }


    public CancellationEligibilityDto checkEligibility(Long reservationId) {

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found with ID: " + reservationId));

        requireOwnerOrAdmin(r);

        return calculateEligibility(r);
    }
    @Transactional
    public CancellationResponseDto cancel(Long reservationId, CancellationRequestDto dto) {

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found with ID: " + reservationId));

        requireOwnerOrAdmin(r);

        if (cancellationRepository.existsByReservation_ReservationId(reservationId)) {
            throw new IllegalStateException("Reservation has already been cancelled");
        }

        CancellationEligibilityDto eligibility = calculateEligibility(r);
        if (!eligibility.eligible()) {
            throw new IllegalStateException(
                    "Cancellation rejected: " + eligibility.policyReason());
        }


        paymentRepository.findByReservation_ReservationId(reservationId)
                .ifPresent(payment -> {
                    if ("SUCCESS".equalsIgnoreCase(payment.getPaymentStatus())) {
                        payment.setPaymentStatus("REFUNDED");
                        paymentRepository.save(payment);
                    }
                });

        Cancellation c = new Cancellation();
        c.setReservation(r);
        c.setCancellationDate(LocalDateTime.now());
        c.setReason(dto.reason());
        c.setRefundAmount(eligibility.refundAmount());
        c.setStatus("PROCESSED");  // or "PENDING" if refund takes time in real life

        Cancellation saved = cancellationRepository.save(c);
        r.setStatus("CANCELLED");
        reservationRepository.save(r);

        return mapper.toResponseDto(saved);
    }

    private CancellationEligibilityDto calculateEligibility(Reservation r) {
        String status = r.getStatus();
        if (!"CONFIRMED".equalsIgnoreCase(status)) {
            String msg = switch (status.toUpperCase()) {
                case "PENDING" -> "Reservation is unpaid. Just abandon it.";
                case "PICKED_UP" -> "Cannot cancel an active rental.";
                case "COMPLETED" -> "Rental is already completed.";
                case "CANCELLED" -> "Already cancelled.";
                default -> "Cannot cancel at this stage.";
            };
            return new CancellationEligibilityDto(
                    r.getReservationId(), status, false,
                    r.getTotalAmount(), BigDecimal.ZERO, 0, msg);
        }
        LocalDate pickup = r.getPickupDate();
        LocalDate today = LocalDate.now();
        long daysUntilPickup = ChronoUnit.DAYS.between(today, pickup);

        int percentage;
        String reason;

        if (daysUntilPickup >= 2) { // 48+ hours (using days as proxy)
            percentage = 100;
            reason = "Cancelled 48+ hours in advance. 100% refund.";
        } else if (daysUntilPickup == 1) { // 24 to 48 hours
            percentage = 50;
            reason = "Cancelled 24-48 hours in advance. 50% refund.";
        } else if (daysUntilPickup == 0) {
            percentage = 0;
            reason = "Cancelled less than 24 hours before pickup. 0% refund.";
        } else {
            // Past date (shouldn't happen if reservation is CONFIRMED, but just in case)
            percentage = 0;
            reason = "Pickup date has already passed.";
        }

        BigDecimal total = r.getTotalAmount() == null ? BigDecimal.ZERO : r.getTotalAmount();
        BigDecimal refund = total.multiply(BigDecimal.valueOf(percentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return new CancellationEligibilityDto(
                r.getReservationId(), status, true, total, refund, percentage, reason);
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
            throw new AccessDeniedException("You can only cancel your own reservations");
        }
    }
}