package com.roadReady.roadready_backend.mapper;

import org.springframework.stereotype.Component;

import com.roadReady.roadready_backend.dto.CarRequestDto;
import com.roadReady.roadready_backend.dto.CarResponseDto;
import com.roadReady.roadready_backend.model.Car;

@Component
public class CarMapper {

    public Car toEntity(CarRequestDto dto) {
        Car car = new Car();
        car.setCarName(dto.carName());
        car.setBrand(dto.brand());
        car.setModel(dto.model());
        car.setFuelType(dto.fuelType());
        car.setSeatingCapacity(dto.seatingCapacity());
        car.setDailyRate(dto.dailyRate());
        car.setStatus(dto.status() == null ? "AVAILABLE" : dto.status().toUpperCase());
        car.setLicensePlate(dto.licensePlate());
        return car;
    }

    public void updateEntity(Car car, CarRequestDto dto) {
        if (dto.carName()         != null) car.setCarName(dto.carName());
        if (dto.brand()           != null) car.setBrand(dto.brand());
        if (dto.model()           != null) car.setModel(dto.model());
        if (dto.fuelType()        != null) car.setFuelType(dto.fuelType());
        if (dto.seatingCapacity() != null) car.setSeatingCapacity(dto.seatingCapacity());
        if (dto.dailyRate()       != null) car.setDailyRate(dto.dailyRate());
        if (dto.status()          != null) car.setStatus(dto.status().toUpperCase());
        if (dto.licensePlate()    != null) car.setLicensePlate(dto.licensePlate());
    }

    public CarResponseDto toResponseDto(Car car) {
        return new CarResponseDto(
                car.getCarId(),
                car.getCarName(),
                car.getBrand(),
                car.getModel(),
                car.getFuelType(),
                car.getSeatingCapacity(),
                car.getDailyRate(),
                car.getStatus(),
                car.getLicensePlate()
        );
    }
}