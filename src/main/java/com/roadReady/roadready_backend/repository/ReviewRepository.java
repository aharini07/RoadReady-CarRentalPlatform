package com.roadReady.roadready_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.roadReady.roadready_backend.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByReservation_ReservationId(Long reservationId);

    List<Review> findByReservation_Car_CarIdOrderByReviewDateDesc(Long carId);

    // Calculate average and count in one query — avoids loading all rows
    @Query("""
        SELECT AVG(r.rating) FROM Review r
        WHERE r.reservation.car.carId = :carId
        """)
    Double findAverageRatingByCarId(@Param("carId") Long carId);

    long countByReservation_Car_CarId(Long carId);
}