package com.roadReady.roadready_backend.config;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.roadReady.roadready_backend.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.roadReady.roadready_backend.dto.ErrorDto;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCustomerNotFound(CustomerNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(e.getMessage(),
                        "Customer was not found in the database",
                        Instant.now()));
    }

    @ExceptionHandler(RentalAgentNotFoundException.class)
    public ResponseEntity<ErrorDto> handleRentalAgentNotFound(RentalAgentNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(e.getMessage(),
                        "Rental agent was not found in the database",
                        Instant.now()));
    }

    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<ErrorDto> handleAdminNotFound(AdminNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(e.getMessage(),
                        "Admin was not found in the database",
                        Instant.now()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(e.getMessage(),
                        "User was not found in the database",
                        Instant.now()));
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ErrorDto> handleDuplicateUsername(DuplicateUsernameException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDto(e.getMessage(),
                        "Please use a different username",
                        Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException e) {

        Map<String, String> errors = new HashMap<>();
        List<org.springframework.validation.FieldError> fieldErrors =
                e.getBindingResult().getFieldErrors();
        fieldErrors.forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorDto("Access denied",
                        e.getMessage(),
                        Instant.now()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDto> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorDto("Invalid credentials",
                        "Username or password is incorrect",
                        Instant.now()));
    }

    // Catch-all for IllegalArgumentException — your services throw these
    // for "user not found", "role mismatch", etc.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(e.getMessage(),
                        "Invalid request",
                        Instant.now()));
    }
    @ExceptionHandler(CarNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCarNotFound(CarNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(e.getMessage(),
                        "Car was not found in the database",
                        Instant.now()));
    }
    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ErrorDto> handleReservationNotFound(ReservationNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(e.getMessage(),
                        "Reservation was not found in the database",
                        Instant.now()));
    }
    //OVERLAP REJECTION RESERVATION
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDto> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDto(e.getMessage(), "Conflict", Instant.now()));
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorDto> handlePaymentNotFound(PaymentNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(e.getMessage(),
                        "Payment was not found in the database",
                        Instant.now()));
    }

    @ExceptionHandler(CheckInOutNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCheckInOutNotFound(CheckInOutNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(e.getMessage(),
                        "Check-in/out record was not found in the database",
                        Instant.now()));
    }
}