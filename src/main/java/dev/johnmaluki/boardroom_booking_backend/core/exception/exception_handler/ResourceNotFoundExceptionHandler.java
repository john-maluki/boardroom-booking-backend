package dev.johnmaluki.boardroom_booking_backend.core.exception.exception_handler;

import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundErrorMessage;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ResourceNotFoundExceptionHandler {
    @ExceptionHandler(value = {ResourceNotFoundException.class})
    public ResponseEntity<Object> resourceNotFoundExceptionHandler(
            ResourceNotFoundException exception
    ) {
        ResourceNotFoundErrorMessage resourceNotFoundErrorMessage = ResourceNotFoundErrorMessage.builder()
                .message(exception.getMessage())
                .httpStatus(HttpStatus.NOT_FOUND)
                .throwable(exception.getCause())
                .build();
        return new ResponseEntity<>(resourceNotFoundErrorMessage, HttpStatus.NOT_FOUND);
    }
}
