package com.roadReady.roadready_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.roadReady.roadready_backend.model.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByReservation_ReservationId(Long reservationId);

    Optional<Payment> findByReservation_ReservationId(Long reservationId);
    // Add this to PaymentRepository.java
    List<Payment> findByReservation_Customer_Users_Username(String username);
}