package com.roadReady.roadready_backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.roadReady.roadready_backend.dto.PricingRequestDto;
import com.roadReady.roadready_backend.dto.PricingResponseDto;
import com.roadReady.roadready_backend.exception.CarNotFoundException;
import com.roadReady.roadready_backend.model.Car;
import com.roadReady.roadready_backend.repository.CarRepository;

@Service
public class PricingService {


    private static final BigDecimal TAX_RATE = new BigDecimal("0.05"); // 5% GST

    private final CarRepository carRepository;

    public PricingService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public PricingResponseDto estimate(PricingRequestDto dto) {

        if (dto.pickupDate() == null || dto.dropoffDate() == null) {
            throw new IllegalArgumentException(
                    "pickupDate and dropoffDate are required");
        }
        if (dto.dropoffDate().isBefore(dto.pickupDate())) {
            throw new IllegalArgumentException(
                    "dropoffDate cannot be before pickupDate");
        }

        Car car = carRepository.findById(dto.carId())
                .orElseThrow(() -> new CarNotFoundException(
                        "Car not found with ID: " + dto.carId()));

        long days = ChronoUnit.DAYS.between(dto.pickupDate(), dto.dropoffDate());
        if (days <= 0) days = 1;   // same-day = 1 day

        BigDecimal subtotal = car.getDailyRate()
                .multiply(BigDecimal.valueOf(days));

        BigDecimal tax = subtotal.multiply(TAX_RATE)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);

        return new PricingResponseDto(
                car.getCarId(),
                car.getCarName(),
                car.getDailyRate(),
                days,
                subtotal,
                tax,
                total
        );
    }
}