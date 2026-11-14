package com.roadReady.roadready_backend.service;

import com.roadReady.roadready_backend.dto.PickupRequestDto;
import com.roadReady.roadready_backend.dto.ReturnRequestDto;
import com.roadReady.roadready_backend.mapper.CheckInOutMapper;
import com.roadReady.roadready_backend.model.Car;
import com.roadReady.roadready_backend.model.CheckInOut;
import com.roadReady.roadready_backend.model.Customer;
import com.roadReady.roadready_backend.model.RentalAgent;
import com.roadReady.roadready_backend.model.Reservation;
import com.roadReady.roadready_backend.model.Users;
import com.roadReady.roadready_backend.repository.CarRepository;
import com.roadReady.roadready_backend.repository.CheckInOutRepository;
import com.roadReady.roadready_backend.repository.RentalAgentRepository;
import com.roadReady.roadready_backend.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckInOutServiceTest {

    @InjectMocks
    private CheckInOutService checkInOutService;

    @Mock private CheckInOutRepository checkInOutRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private RentalAgentRepository rentalAgentRepository;
    @Mock private CarRepository carRepository;
    @Mock private CheckInOutMapper mapper;

    private RentalAgent mockAgent;
    private Reservation mockReservation;
    private Car mockCar;
    private Customer mockCustomer;
    private Users mockUser;

    @BeforeEach
    public void setUp() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("agent1");
        SecurityContextHolder.setContext(securityContext);


        mockUser = new Users();
        mockUser.setUsername("agent1");

        mockAgent = new RentalAgent();
        mockAgent.setAgentId(1L);
        mockAgent.setName("Agent Smith");
        mockAgent.setUsers(mockUser);

        // 3. Create a fake Customer
        Users customerUser = new Users();
        customerUser.setUsername("customer1");
        mockCustomer = new Customer();
        mockCustomer.setUsers(customerUser);

        // 4. Create a fake Car
        mockCar = new Car();
        mockCar.setCarId(10L);
        mockCar.setCarName("Creta");
        mockCar.setStatus("AVAILABLE");

        // 5. Create a fake Reservation
        mockReservation = new Reservation();
        mockReservation.setReservationId(100L);
        mockReservation.setStatus("CONFIRMED");
        mockReservation.setCar(mockCar);
        mockReservation.setCustomer(mockCustomer);
        mockReservation.setTotalAmount(new BigDecimal("1500.00"));
    }

    @Test
    public void recordPickupTest_Success() {
        PickupRequestDto dto = new PickupRequestDto("Good", "Full", "No issues");

        when(rentalAgentRepository.findAll()).thenReturn(List.of(mockAgent));
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(mockReservation));
        when(checkInOutRepository.existsByReservation_ReservationId(100L)).thenReturn(false);

        CheckInOut mockSavedCheckInOut = new CheckInOut();
        when(checkInOutRepository.save(any(CheckInOut.class))).thenReturn(mockSavedCheckInOut);

        checkInOutService.recordPickup(100L, dto);

        // Verify the Reservation status changed to PICKED_UP
        assertEquals("PICKED_UP", mockReservation.getStatus());
        // Verify the Car status changed to RENTED
        assertEquals("RENTED", mockCar.getStatus());

        verify(checkInOutRepository, times(1)).save(any(CheckInOut.class));
        verify(reservationRepository, times(1)).save(mockReservation);
        verify(carRepository, times(1)).save(mockCar);
    }


    @Test
    public void recordPickupTest_NotConfirmed() {
        mockReservation.setStatus("PENDING"); // Invalid status
        PickupRequestDto dto = new PickupRequestDto("Good", "Full", "No issues");

        when(rentalAgentRepository.findAll()).thenReturn(List.of(mockAgent));
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(mockReservation));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            checkInOutService.recordPickup(100L, dto);
        });

        assertTrue(exception.getMessage().contains("only CONFIRMED can be picked up"));
        verify(checkInOutRepository, never()).save(any());
    }

    @Test
    public void recordReturnTest_Success_WithPenalty() {
        mockReservation.setStatus("PICKED_UP"); // Must be picked up to return

        CheckInOut existingCheckInOut = new CheckInOut();
        existingCheckInOut.setReservation(mockReservation);

        // Apply a ₹500 penalty
        ReturnRequestDto dto = new ReturnRequestDto("Damaged", "Half", "Bumper scratched", new BigDecimal("500.00"), "Bumper scratched");

        when(rentalAgentRepository.findAll()).thenReturn(List.of(mockAgent));
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(mockReservation));
        when(checkInOutRepository.findByReservation_ReservationId(100L)).thenReturn(Optional.of(existingCheckInOut));
        when(checkInOutRepository.save(any(CheckInOut.class))).thenReturn(existingCheckInOut);

        checkInOutService.recordReturn(100L, dto);

        // Verify Reservation status changed to COMPLETED
        assertEquals("COMPLETED", mockReservation.getStatus());
        // Verify Penalty was added (1500 + 500 = 2000)
        assertEquals(new BigDecimal("2000.00"), mockReservation.getTotalAmount());
        // Verify Car status changed to MAINTENANCE (because of penalty)
        assertEquals("MAINTENANCE", mockCar.getStatus());

        verify(carRepository, times(1)).save(mockCar);
    }


    @Test
    public void recordReturnTest_NoPickupRecord() {
        mockReservation.setStatus("PICKED_UP");
        ReturnRequestDto dto = new ReturnRequestDto("Good", "Full", "Ok", BigDecimal.ZERO, "");

        when(rentalAgentRepository.findAll()).thenReturn(List.of(mockAgent));
        when(reservationRepository.findById(100L)).thenReturn(Optional.of(mockReservation));
        when(checkInOutRepository.findByReservation_ReservationId(100L)).thenReturn(Optional.empty());

        assertThrows(com.roadReady.roadready_backend.exception.CheckInOutNotFoundException.class, () -> {
            checkInOutService.recordReturn(100L, dto);
        });

        verify(reservationRepository, never()).save(any());
    }
}