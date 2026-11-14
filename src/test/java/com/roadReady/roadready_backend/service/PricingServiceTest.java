package com.roadReady.roadready_backend.service;

import com.roadReady.roadready_backend.dto.PricingRequestDto;
import com.roadReady.roadready_backend.dto.PricingResponseDto;
import com.roadReady.roadready_backend.exception.CarNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PricingServiceTest {

    @InjectMocks
    private PricingService pricingService;

    @Mock
    private CarRepository carRepository;


    @Test
    public void estimateTest_Valid() {
        Car car = new Car();
        car.setCarId(1L);
        car.setCarName("Creta");
        car.setDailyRate(new BigDecimal("1500.00"));

        PricingRequestDto dto = new PricingRequestDto(
                1L,
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 13) // 3 days
        );

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        PricingResponseDto response = pricingService.estimate(dto);

        assertEquals(new BigDecimal("4500.00"), response.subtotal());
        assertEquals(new BigDecimal("225.00"), response.tax()); // 5% tax
        assertEquals(new BigDecimal("4725.00"), response.totalAmount());
        assertEquals(3, response.durationDays());

        verify(carRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(carRepository);
    }


    @Test
    public void estimateTest_DropoffBeforePickup() {
        PricingRequestDto dto = new PricingRequestDto(
                1L,
                LocalDate.of(2026, 11, 13),
                LocalDate.of(2026, 11, 10) // Invalid
        );

        assertThrows(IllegalArgumentException.class, () -> pricingService.estimate(dto));
        verify(carRepository, never()).findById(any());
    }


    @Test
    public void estimateTest_CarNotFound() {
        PricingRequestDto dto = new PricingRequestDto(99L, LocalDate.now(), LocalDate.now().plusDays(2));
        when(carRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CarNotFoundException.class, () -> pricingService.estimate(dto));
        verify(carRepository, times(1)).findById(99L);
    }
}