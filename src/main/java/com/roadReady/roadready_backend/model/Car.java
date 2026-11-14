package com.roadReady.roadready_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "car")
@Getter
@Setter
@NoArgsConstructor
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Long carId;

    @Column(name = "car_name", nullable = false, length = 50)
    private String carName;

    @Column(name = "brand", length = 50)
    private String brand;

    @Column(name = "model", length = 50)
    private String model;

    // Stored as text here to match the database ENUM column.
    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "seating_capacity")
    private Integer seatingCapacity;

    @Column(name = "daily_rate", precision = 10, scale = 2)
    private BigDecimal dailyRate;

    // Stored as text here to match the database ENUM column.
    @Column(name = "status")
    private String status;
    @Column(name = "license_plate", length = 20, unique = true)
    private String licensePlate;
}
