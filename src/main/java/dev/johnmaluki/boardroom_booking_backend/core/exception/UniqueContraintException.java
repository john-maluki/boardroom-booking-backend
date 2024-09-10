package dev.johnmaluki.boardroom_booking_backend.core.exception;

public class UniqueContraintException extends RuntimeException{
    public UniqueContraintException(String message) {
        super(message);
    }
}
