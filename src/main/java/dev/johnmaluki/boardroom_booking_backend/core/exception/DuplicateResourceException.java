package dev.johnmaluki.boardroom_booking_backend.core.exception;

public class DuplicateResourceException extends RuntimeException{
    public DuplicateResourceException(String message) {
        super(message);
    }
}
