package com.roadReady.roadready_backend.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.roadReady.roadready_backend.dto.CarRequestDto;
import com.roadReady.roadready_backend.dto.CarResponseDto;
import com.roadReady.roadready_backend.dto.PageResponseDto;
import com.roadReady.roadready_backend.service.CarService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponseDto<CarResponseDto>> searchCars(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) Integer minSeats,
            @RequestParam(required = false) Integer maxSeats,
            @RequestParam(required = false) BigDecimal minRate,
            @RequestParam(required = false) BigDecimal maxRate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "carId") String sortBy,
            @RequestParam(defaultValue = "asc")   String sortDir) {

        return ResponseEntity.ok(carService.searchCars(
                brand, fuelType, minSeats, maxSeats,
                minRate, maxRate, status,
                page, size, sortBy, sortDir));
    }

    @GetMapping("/available")
    public ResponseEntity<PageResponseDto<CarResponseDto>> availableCars(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
            @RequestParam(required = false, defaultValue = "1") Integer passengers,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "dailyRate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        return ResponseEntity.ok(carService.findAvailableCars(
                pickupDate, returnDate, passengers,
                page, size, sortBy, sortDir));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CarResponseDto>> getAllCars() {
        return ResponseEntity.ok(carService.getAllCars());
    }

    @GetMapping("/{carId}")
    public ResponseEntity<CarResponseDto> getCarById(@PathVariable Long carId) {
        return ResponseEntity.ok(carService.getCarById(carId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CarResponseDto> createCar(
            @Valid @RequestBody CarRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(carService.createCar(dto));
    }

    @PutMapping("/{carId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CarResponseDto> updateCar(
            @PathVariable Long carId,
            @Valid @RequestBody CarRequestDto dto) {
        return ResponseEntity.ok(carService.updateCar(carId, dto));
    }

    @DeleteMapping("/{carId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCar(@PathVariable Long carId) {
        carService.deleteCar(carId);
        return ResponseEntity.noContent().build();
    }
}