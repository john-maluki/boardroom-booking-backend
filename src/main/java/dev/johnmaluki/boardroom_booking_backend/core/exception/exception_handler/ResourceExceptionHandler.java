package dev.johnmaluki.boardroom_booking_backend.core.exception.exception_handler;

import dev.johnmaluki.boardroom_booking_backend.core.exception.JwtTokenException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceErrorMessage;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceOwnershipException;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ResourceExceptionHandler {
    @ExceptionHandler(value = {ResourceNotFoundException.class})
    public ResponseEntity<Object> resourceNotFoundExceptionHandler(
            ResourceNotFoundException exception
    ) {
        ResourceErrorMessage errorMessage = ResourceErrorMessage.builder()
                .message(exception.getMessage())
                .httpStatus(HttpStatus.NOT_FOUND)
                .throwable(exception.getCause())
                .build();
        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {ResourceOwnershipException.class})
    public ResponseEntity<Object> resourceAccessExceptionHandler(
            ResourceOwnershipException exception
    ) {
        ResourceErrorMessage errorMessage = ResourceErrorMessage.builder()
                .message(exception.getMessage())
                .httpStatus(HttpStatus.FORBIDDEN)
                .throwable(exception.getCause())
                .build();
        return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(JwtTokenException.class)
    public ResponseEntity<Object> jwtTokenExceptionHandler(
            JwtTokenException exception
    ) {
        ResourceErrorMessage errorMessage = ResourceErrorMessage.builder()
                .message(exception.getMessage())
                .httpStatus(HttpStatus.FORBIDDEN)
                .throwable(exception.getCause())
                .build();
        return new ResponseEntity<>(errorMessage, HttpStatus.FORBIDDEN);
    }
}
