package com.roadReady.roadready_backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.roadReady.roadready_backend.dto.CarRequestDto;
import com.roadReady.roadready_backend.dto.CarResponseDto;
import com.roadReady.roadready_backend.dto.PageResponseDto;
import com.roadReady.roadready_backend.exception.CarNotFoundException;
import com.roadReady.roadready_backend.mapper.CarMapper;
import com.roadReady.roadready_backend.model.Car;
import com.roadReady.roadready_backend.repository.CarRepository;

@Service
public class CarService {

    private static final int MAX_PAGE_SIZE = 100;

    private final CarRepository carRepository;
    private final CarMapper carMapper;

    public CarService(CarRepository carRepository, CarMapper carMapper) {
        this.carRepository = carRepository;
        this.carMapper = carMapper;
    }


    public PageResponseDto<CarResponseDto> searchCars(
            String brand, String fuelType,
            Integer minSeats, Integer maxSeats,
            java.math.BigDecimal minRate, java.math.BigDecimal maxRate,
            String status,
            int page, int size,
            String sortBy, String sortDir) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Page<Car> result = carRepository.searchCars(
                blankToNull(brand), blankToNull(fuelType),
                minSeats, maxSeats, minRate, maxRate,
                blankToNull(status), pageable);

        return toPageResponse(result);
    }

    // AVAILABILITY
    public PageResponseDto<CarResponseDto> findAvailableCars(
            LocalDate pickupDate, LocalDate returnDate, Integer passengers,
            int page, int size, String sortBy, String sortDir) {

        if (pickupDate == null || returnDate == null) {
            throw new IllegalArgumentException(
                    "pickupDate and returnDate are required");
        }
        if (!pickupDate.isBefore(returnDate)) {
            throw new IllegalArgumentException(
                    "pickupDate must be before returnDate");
        }
        if (passengers != null && passengers < 1) {
            throw new IllegalArgumentException("passengers must be >= 1");
        }

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Page<Car> result = carRepository.findAvailableCars(
                pickupDate, returnDate, passengers == null ? 1 : passengers,
                pageable);

        return toPageResponse(result);
    }

    public CarResponseDto getCarById(Long carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException(
                        "Car not found with ID: " + carId));
        return carMapper.toResponseDto(car);
    }

    public List<CarResponseDto> getAllCars() {
        return carRepository.findAll().stream()
                .map(carMapper::toResponseDto).toList();
    }

    public CarResponseDto createCar(CarRequestDto dto) {
        Car car = carMapper.toEntity(dto);
        return carMapper.toResponseDto(carRepository.save(car));
    }

    public CarResponseDto updateCar(Long carId, CarRequestDto dto) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException(
                        "Car not found with ID: " + carId));
        carMapper.updateEntity(car, dto);
        return carMapper.toResponseDto(carRepository.save(car));
    }

    public void deleteCar(Long carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException(
                        "Car not found with ID: " + carId));
        carRepository.delete(car);
    }

    private Pageable buildPageable(int page, int size,
                                   String sortBy, String sortDir) {
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > MAX_PAGE_SIZE) size = MAX_PAGE_SIZE;


        String sortField = switch (sortBy == null ? "carId" : sortBy) {
            case "dailyRate", "price"        -> "dailyRate";
            case "seatingCapacity", "seats"  -> "seatingCapacity";
            case "brand"                     -> "brand";
            case "carName"                   -> "carName";
            default                          -> "carId";
        };

        Sort.Direction dir = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(dir, sortField));
    }

    private PageResponseDto<CarResponseDto> toPageResponse(Page<Car> page) {
        List<CarResponseDto> content = page.getContent().stream()
                .map(carMapper::toResponseDto).toList();

        return new PageResponseDto<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private String blankToNull(String s) {

        return (s == null || s.isBlank()) ? null : s.trim();
    }
}