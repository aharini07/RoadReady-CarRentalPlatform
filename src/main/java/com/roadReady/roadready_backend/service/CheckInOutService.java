package com.roadReady.roadready_backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.roadReady.roadready_backend.dto.CheckInOutResponseDto;
import com.roadReady.roadready_backend.dto.InspectionReportDto;
import com.roadReady.roadready_backend.dto.PickupRequestDto;
import com.roadReady.roadready_backend.dto.ReturnRequestDto;
import com.roadReady.roadready_backend.exception.CheckInOutNotFoundException;
import com.roadReady.roadready_backend.exception.RentalAgentNotFoundException;
import com.roadReady.roadready_backend.exception.ReservationNotFoundException;
import com.roadReady.roadready_backend.mapper.CheckInOutMapper;
import com.roadReady.roadready_backend.model.Car;
import com.roadReady.roadready_backend.model.CheckInOut;
import com.roadReady.roadready_backend.model.RentalAgent;
import com.roadReady.roadready_backend.model.Reservation;
import com.roadReady.roadready_backend.repository.CarRepository;
import com.roadReady.roadready_backend.repository.CheckInOutRepository;
import com.roadReady.roadready_backend.repository.RentalAgentRepository;
import com.roadReady.roadready_backend.repository.ReservationRepository;

@Slf4j
@Service
public class CheckInOutService {

    private final CheckInOutRepository checkInOutRepository;
    private final ReservationRepository reservationRepository;
    private final RentalAgentRepository rentalAgentRepository;
    private final CarRepository carRepository;
    private final CheckInOutMapper mapper;

    public CheckInOutService(CheckInOutRepository checkInOutRepository,
                             ReservationRepository reservationRepository,
                             RentalAgentRepository rentalAgentRepository,
                             CarRepository carRepository,
                             CheckInOutMapper mapper) {
        this.checkInOutRepository = checkInOutRepository;
        this.reservationRepository = reservationRepository;
        this.rentalAgentRepository = rentalAgentRepository;
        this.carRepository = carRepository;
        this.mapper = mapper;
    }
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncCarInventoryOnStartup() {
        // REPLACED System.out.println
        log.info("Syncing car inventory with active reservations...");

        List<Car> allCars = carRepository.findAll();
        for (Car car : allCars) {
            if (!"MAINTENANCE".equalsIgnoreCase(car.getStatus())) {
                car.setStatus("AVAILABLE");
            }
        }
        carRepository.saveAll(allCars);

        List<Reservation> allReservations = reservationRepository.findAll();
        for (Reservation r : allReservations) {
            if ("PICKED_UP".equalsIgnoreCase(r.getStatus())) {
                Car car = r.getCar();
                car.setStatus("RENTED");
                carRepository.save(car);
            }
        }
        log.info("ok");
    }
    @Transactional
    public CheckInOutResponseDto recordPickup(Long reservationId, PickupRequestDto dto) {
        log.info("Attempting check-in (pickup) for reservation ID: {}", reservationId);

        RentalAgent agent = currentAgent();

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("Check-in failed: Reservation not found with ID: {}", reservationId);
                    return new ReservationNotFoundException("Reservation not found with ID: " + reservationId);
                });

        if (!"CONFIRMED".equalsIgnoreCase(r.getStatus())) {
            log.warn("Check-in failed: Reservation {} status is {} (must be CONFIRMED)", reservationId, r.getStatus());
            throw new IllegalStateException(
                    "Cannot hand over car. Reservation status is "
                            + r.getStatus() + "; only CONFIRMED can be picked up.");
        }

        if (checkInOutRepository.existsByReservation_ReservationId(reservationId)) {
            log.warn("Check-in failed: Pickup already recorded for reservation {}", reservationId);
            throw new IllegalStateException(
                    "Pickup has already been recorded for reservation " + reservationId);
        }

        CheckInOut c = new CheckInOut();
        c.setReservation(r);
        c.setAgent(agent);
        c.setPickupCondition(dto.pickupCondition());
        c.setFuelLevelBefore(dto.fuelLevelBefore());
        c.setRemarks(dto.remarks());
        c.setCheckinDate(LocalDateTime.now());

        CheckInOut saved = checkInOutRepository.save(c);

        // 1. Flip reservation -> PICKED_UP
        r.setStatus("PICKED_UP");
        reservationRepository.save(r);

        // 2. Update Car status to RENTED
        Car car = r.getCar();
        car.setStatus("RENTED");
        carRepository.save(car);
        log.info("Check-in successful: Car {} handed over to {}. New status: PICKED_UP",
                car.getCarName(), r.getCustomer().getUsers().getUsername());

        return mapper.toResponseDto(saved);
    }


    @Transactional
    public CheckInOutResponseDto recordReturn(Long reservationId, ReturnRequestDto dto) {

        log.info("Attempting check-out (return) for reservation ID: {}", reservationId);

        RentalAgent agent = currentAgent();

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("Check-out failed: Reservation not found with ID: {}", reservationId);
                    return new ReservationNotFoundException("Reservation not found with ID: " + reservationId);
                });

        if (!"PICKED_UP".equalsIgnoreCase(r.getStatus())) {
            log.warn("Check-out failed: Reservation {} status is {} (must be PICKED_UP)", reservationId, r.getStatus());
            throw new IllegalStateException(
                    "Cannot accept return. Reservation status is "
                            + r.getStatus() + "; only PICKED_UP can be returned.");
        }

        CheckInOut c = checkInOutRepository.findByReservation_ReservationId(reservationId)
                .orElseThrow(() -> {
                    log.error("Check-out failed: No pickup record for reservation {}", reservationId);
                    return new CheckInOutNotFoundException("No pickup record found for reservation " + reservationId);
                });

        c.setAgent(agent);
        c.setReturnCondition(dto.returnCondition());
        c.setFuelLevelAfter(dto.fuelLevelAfter());
        c.setCheckoutDate(LocalDateTime.now());
        if (dto.remarks() != null && !dto.remarks().isBlank()) {
            c.setRemarks(dto.remarks());
        }

        CheckInOut saved = checkInOutRepository.save(c);

        // 1. Apply Penalty Fee if any
        if (dto.penaltyFee() != null && dto.penaltyFee().compareTo(BigDecimal.ZERO) > 0) {
            log.info("Applying penalty fee of ₹{} for reservation {}", dto.penaltyFee(), reservationId);
            r.setPenaltyFee(dto.penaltyFee());
            r.setTotalAmount(r.getTotalAmount().add(dto.penaltyFee()));
        }

        // 2. Mark reservation as COMPLETED
        r.setStatus("COMPLETED");
        reservationRepository.save(r);

        // 3. Update Car status
        Car car = r.getCar();
        if (dto.penaltyFee() != null && dto.penaltyFee().compareTo(BigDecimal.ZERO) > 0) {
            car.setStatus("MAINTENANCE");
            log.warn("Car {} set to MAINTENANCE due to damage penalty.", car.getCarName());
        } else {
            car.setStatus("AVAILABLE");
        }
        carRepository.save(car);

        // ADDED LOGGER
        log.info("Check-out successful: Car {} returned. New reservation status: COMPLETED", car.getCarName());

        return mapper.toResponseDto(saved);
    }

    //REPORT
    public InspectionReportDto getReport(Long reservationId) {

        RentalAgent agent = currentAgent(); // Validates the caller
        log.info("Generating inspection report for reservation {} requested by agent {}", reservationId, agent.getName());

        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found with ID: " + reservationId));

        CheckInOut c = checkInOutRepository.findByReservation_ReservationId(reservationId)
                .orElseThrow(() -> new CheckInOutNotFoundException(
                        "No check-in/out record for reservation " + reservationId));

        boolean damage = c.getReturnCondition() != null
                && c.getPickupCondition() != null
                && !normalize(c.getPickupCondition())
                .equals(normalize(c.getReturnCondition()));

        boolean fuelShortage = c.getFuelLevelBefore() != null
                && "FULL".equalsIgnoreCase(c.getFuelLevelBefore())
                && c.getFuelLevelAfter() != null
                && !"FULL".equalsIgnoreCase(c.getFuelLevelAfter());

        return new InspectionReportDto(
                r.getReservationId(),
                r.getCar().getCarId(),
                r.getCar().getCarName(),
                r.getCustomer().getUsers().getUsername(),
                r.getStatus(),

                c.getPickupCondition(),
                c.getReturnCondition(),
                damage,

                c.getFuelLevelBefore(),
                c.getFuelLevelAfter(),
                fuelShortage,

                c.getAgent() == null ? null : c.getAgent().getAgentId(),
                c.getAgent() == null ? null : c.getAgent().getName(),
                c.getCheckinDate(),
                c.getCheckoutDate(),

                null,
                c.getRemarks()
        );
    }


    private String normalize(String s) {
        return s == null ? null : s.trim().toLowerCase();
    }

    private RentalAgent currentAgent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || "anonymousUser".equals(auth.getName())) {
            log.error("Authentication required to perform this action.");
            throw new AccessDeniedException("Authentication required");
        }

        String username = auth.getName();

        return rentalAgentRepository.findAll().stream()
                .filter(a -> a.getUsers() != null
                        && Objects.equals(
                        a.getUsers().getUsername(), username))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Rental agent profile not found for user: {}", username);
                    return new RentalAgentNotFoundException(
                            "You do not have a rental-agent profile");
                });
    }
}