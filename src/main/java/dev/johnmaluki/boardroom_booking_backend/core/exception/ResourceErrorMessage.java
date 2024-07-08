package dev.johnmaluki.boardroom_booking_backend.core.exception;

import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record ResourceErrorMessage(String message, Throwable throwable, HttpStatus httpStatus) {
}
