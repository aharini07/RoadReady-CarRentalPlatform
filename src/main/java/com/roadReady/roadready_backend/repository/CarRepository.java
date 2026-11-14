package com.roadReady.roadready_backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.roadReady.roadready_backend.model.Car;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    // ─────────────────────── SEARCH ───────────────────────
    @Query("""
        SELECT c FROM Car c
        WHERE (:brand       IS NULL OR LOWER(c.brand)    LIKE LOWER(CONCAT('%', :brand, '%')))
          AND (:fuelType    IS NULL OR LOWER(c.fuelType) = LOWER(:fuelType))
          AND (:minSeats    IS NULL OR c.seatingCapacity >= :minSeats)
          AND (:maxSeats    IS NULL OR c.seatingCapacity <= :maxSeats)
          AND (:minRate     IS NULL OR c.dailyRate       >= :minRate)
          AND (:maxRate     IS NULL OR c.dailyRate       <= :maxRate)
          AND (:status      IS NULL OR LOWER(c.status)   = LOWER(:status))
        """)
    Page<Car> searchCars(
            @Param("brand")    String brand,
            @Param("fuelType") String fuelType,
            @Param("minSeats") Integer minSeats,
            @Param("maxSeats") Integer maxSeats,
            @Param("minRate")  java.math.BigDecimal minRate,
            @Param("maxRate")  java.math.BigDecimal maxRate,
            @Param("status")   String status,
            Pageable pageable
    );

    // ─────────────────────── AVAILABILITY ───────────────────────
    // Return cars that are AVAILABLE and have NO overlapping reservation
    // Only checks against active bookings (PENDING, CONFIRMED, PICKED_UP)
    @Query("""
        SELECT c FROM Car c
        WHERE LOWER(c.status) = 'available'
          AND c.seatingCapacity >= :passengers
          AND NOT EXISTS (
              SELECT r FROM Reservation r
              WHERE r.car = c
                AND UPPER(r.status) IN ('PENDING', 'CONFIRMED', 'PICKED_UP')
                AND r.pickupDate  <= :returnDate
                AND r.dropoffDate >= :pickupDate
          )
        """)
    Page<Car> findAvailableCars(
            @Param("pickupDate") LocalDate pickupDate,
            @Param("returnDate") LocalDate returnDate,
            @Param("passengers") Integer passengers,
            Pageable pageable
    );

    // Count active reservations on a car (used by update/delete guards if needed)
    long countByCarId(Long carId);

    // Existence check for duplicate seed data
    boolean existsByCarNameAndBrandAndModel(String carName, String brand, String model);
}