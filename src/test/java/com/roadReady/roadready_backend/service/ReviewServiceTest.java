package com.roadReady.roadready_backend.service;

import com.roadReady.roadready_backend.dto.ReviewRequestDto;
import com.roadReady.roadready_backend.mapper.ReviewMapper;
import com.roadReady.roadready_backend.model.Reservation;
import com.roadReady.roadready_backend.model.Users;
import com.roadReady.roadready_backend.model.Customer;
import com.roadReady.roadready_backend.repository.CarRepository;
import com.roadReady.roadready_backend.repository.ReservationRepository;
import com.roadReady.roadready_backend.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock private ReviewRepository reviewRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private CarRepository carRepository;
    @Mock private ReviewMapper mapper;

    private void mockSecurityContext() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void createReviewTest_NotCompletedRejected() {
        mockSecurityContext();

        Users user = new Users();
        user.setUsername("testuser");
        Customer customer = new Customer();
        customer.setUsers(user);

        Reservation r = new Reservation();
        r.setReservationId(1L);
        r.setStatus("PENDING");
        r.setCustomer(customer);

        ReviewRequestDto dto = new ReviewRequestDto(1L, 5, "Great car", false);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            reviewService.createReview(dto);
        });

        assertTrue(exception.getMessage().contains("only COMPLETED rentals can be reviewed"));
        verify(reviewRepository, never()).save(any());
    }
}