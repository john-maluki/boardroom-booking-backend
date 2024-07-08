package dev.johnmaluki.boardroom_booking_backend.core.exception;

public class ResourceOwnershipException extends RuntimeException{
    public ResourceOwnershipException(String message) {
        super(message);
    }
}
