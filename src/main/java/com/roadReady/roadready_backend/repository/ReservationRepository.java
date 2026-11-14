package com.roadReady.roadready_backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.roadReady.roadready_backend.model.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // All reservations of one customer, newest first.
    List<Reservation> findByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);

    // Overlap check — only blocks if the existing booking is PENDING, CONFIRMED, or PICKED_UP
    @Query("""
        SELECT COUNT(r) FROM Reservation r
        WHERE r.car.carId = :carId
          AND UPPER(r.status) IN ('PENDING', 'CONFIRMED', 'PICKED_UP')
          AND r.pickupDate  <= :dropoffDate
          AND r.dropoffDate >= :pickupDate
        """)
    long countOverlapping(
            @Param("carId")       Long carId,
            @Param("pickupDate")  LocalDate pickupDate,
            @Param("dropoffDate") LocalDate dropoffDate
    );
}