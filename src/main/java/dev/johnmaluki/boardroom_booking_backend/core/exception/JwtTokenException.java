package dev.johnmaluki.boardroom_booking_backend.core.exception;

public class JwtTokenException extends RuntimeException{
    public JwtTokenException(String message) {
        super(message);
    }
}
