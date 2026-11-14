package com.roadReady.roadready_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.roadReady.roadready_backend.model.CheckInOut;

@Repository
public interface CheckInOutRepository extends JpaRepository<CheckInOut, Long> {

    Optional<CheckInOut> findByReservation_ReservationId(Long reservationId);

    boolean existsByReservation_ReservationId(Long reservationId);
}