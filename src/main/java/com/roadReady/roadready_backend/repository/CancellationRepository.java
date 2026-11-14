package com.roadReady.roadready_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.roadReady.roadready_backend.model.Cancellation;

@Repository
public interface CancellationRepository extends JpaRepository<Cancellation, Long> {

    Optional<Cancellation> findByReservation_ReservationId(Long reservationId);

    boolean existsByReservation_ReservationId(Long reservationId);
}