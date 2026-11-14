package com.roadReady.roadready_backend.service;

import com.roadReady.roadready_backend.dto.CarRequestDto;
import com.roadReady.roadready_backend.dto.CarResponseDto;
import com.roadReady.roadready_backend.exception.CarNotFoundException;
import com.roadReady.roadready_backend.mapper.CarMapper;
import com.roadReady.roadready_backend.model.Car;
import com.roadReady.roadready_backend.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {

    @InjectMocks
    private CarService carService;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarMapper carMapper;

    @Test
    public void findAvailableCarsTest_InvalidDates() {
        // Pickup is AFTER return (invalid)
        LocalDate pickup = LocalDate.of(2026, 11, 15);
        LocalDate returnDate = LocalDate.of(2026, 11, 10);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            carService.findAvailableCars(pickup, returnDate, 2, 0, 10, "dailyRate", "asc");
        });

        assertEquals("pickupDate must be before returnDate", exception.getMessage());
        verify(carRepository, never()).findAvailableCars(any(), any(), any(), any());
    }

    @Test
    public void findAvailableCarsTest_NullDates() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            carService.findAvailableCars(null, LocalDate.now(), 2, 0, 10, "dailyRate", "asc");
        });

        assertEquals("pickupDate and returnDate are required", exception.getMessage());
    }

    @Test
    public void getCarByIdTest_Success() {
        Car mockCar = new Car();
        mockCar.setCarId(1L);
        mockCar.setCarName("Creta");

        CarResponseDto mockResponse = new CarResponseDto(
                1L, "Creta", "Hyundai", "SX", "Petrol", 5, new BigDecimal("1500"), "AVAILABLE", "TN-01-AB-1234"
        );

        when(carRepository.findById(1L)).thenReturn(Optional.of(mockCar));
        when(carMapper.toResponseDto(mockCar)).thenReturn(mockResponse);

        CarResponseDto result = carService.getCarById(1L);

        assertEquals("Creta", result.carName());
        verify(carRepository, times(1)).findById(1L);
    }

    @Test
    public void getCarByIdTest_NotFound() {
        when(carRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CarNotFoundException.class, () -> {
            carService.getCarById(99L);
        });

        verify(carRepository, times(1)).findById(99L);
    }

    @Test
    public void createCarTest_Success() {
        CarRequestDto request = new CarRequestDto(
                "Creta", "Hyundai", "SX", "Petrol", 5, new BigDecimal("1500"), "AVAILABLE", "TN-01-AB-1234"
        );

        Car mockCar = new Car();
        mockCar.setCarId(1L);
        mockCar.setCarName("Creta");

        Car savedCar = new Car();
        savedCar.setCarId(1L);

        CarResponseDto expectedResponse = new CarResponseDto(
                1L, "Creta", "Hyundai", "SX", "Petrol", 5, new BigDecimal("1500"), "AVAILABLE", "TN-01-AB-1234"
        );

        when(carMapper.toEntity(request)).thenReturn(mockCar);
        when(carRepository.save(mockCar)).thenReturn(savedCar);
        when(carMapper.toResponseDto(savedCar)).thenReturn(expectedResponse);

        CarResponseDto result = carService.createCar(request);

        assertEquals(1L, result.carId());
        assertEquals("Creta", result.carName());
        verify(carRepository, times(1)).save(mockCar);
    }

    @Test
    public void updateCarTest_Success() {
        Car existingCar = new Car();
        existingCar.setCarId(1L);
        existingCar.setDailyRate(new BigDecimal("1500"));

        CarRequestDto updateRequest = new CarRequestDto(
                "Creta", "Hyundai", "SX", "Petrol", 5, new BigDecimal("2000"), "AVAILABLE", "TN-01-AB-1234"
        );

        Car updatedCar = new Car();
        updatedCar.setCarId(1L);
        updatedCar.setDailyRate(new BigDecimal("2000"));

        CarResponseDto expectedResponse = new CarResponseDto(
                1L, "Creta", "Hyundai", "SX", "Petrol", 5, new BigDecimal("2000"), "AVAILABLE", "TN-01-AB-1234"
        );

        when(carRepository.findById(1L)).thenReturn(Optional.of(existingCar));
        doNothing().when(carMapper).updateEntity(existingCar, updateRequest);
        when(carRepository.save(existingCar)).thenReturn(updatedCar);
        when(carMapper.toResponseDto(updatedCar)).thenReturn(expectedResponse);

        CarResponseDto result = carService.updateCar(1L, updateRequest);

        assertEquals(new BigDecimal("2000"), result.dailyRate());
        verify(carRepository, times(1)).save(existingCar);
    }

    @Test
    public void deleteCarTest_Success() {
        Car mockCar = new Car();
        mockCar.setCarId(1L);

        when(carRepository.findById(1L)).thenReturn(Optional.of(mockCar));
        doNothing().when(carRepository).delete(mockCar);

        carService.deleteCar(1L);

        verify(carRepository, times(1)).delete(mockCar);
    }

    @Test
    public void deleteCarTest_NotFound() {
        when(carRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CarNotFoundException.class, () -> {
            carService.deleteCar(99L);
        });

        verify(carRepository, never()).delete(any());
    }
}