package com.roadReady.roadready_backend.exception;

public class CheckInOutNotFoundException extends RuntimeException {
    public CheckInOutNotFoundException(String message) {
        super(message);
    }
}
