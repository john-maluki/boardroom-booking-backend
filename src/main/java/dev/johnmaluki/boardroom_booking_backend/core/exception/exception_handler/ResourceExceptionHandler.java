package dev.johnmaluki.boardroom_booking_backend.core.exception.exception_handler;

import dev.johnmaluki.boardroom_booking_backend.core.exception.*;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

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

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Object> duplicateResourceExceptionHandler(
            DuplicateResourceException exception
    ) {
        ResourceErrorMessage errorMessage = ResourceErrorMessage.builder()
                .message(exception.getMessage())
                .httpStatus(HttpStatus.BAD_REQUEST)
                .throwable(exception.getCause())
                .build();
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalidArgumentExceptionHandler(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errorMessage = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(
                error -> errorMessage.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
}
