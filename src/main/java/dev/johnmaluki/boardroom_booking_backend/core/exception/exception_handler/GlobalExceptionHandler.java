package dev.johnmaluki.boardroom_booking_backend.core.exception.exception_handler;

import dev.johnmaluki.boardroom_booking_backend.core.exception.FileUploadException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ReservationOverlapException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceErrorMessage;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceUpdateExeption;
import dev.johnmaluki.boardroom_booking_backend.core.exception.UniqueContraintException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(value = {FileUploadException.class})
  public ResponseEntity<Object> fileUploadExceptionHandler(FileUploadException exception) {
    ResourceErrorMessage errorMessage =
        ResourceErrorMessage.builder()
            .message(exception.getMessage())
            .httpStatus(HttpStatus.BAD_REQUEST)
            .throwable(exception.getCause())
            .build();
    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(value = {UniqueContraintException.class})
  public ResponseEntity<Object> dataIntegrityViolationException(
      UniqueContraintException exception) {
    ResourceErrorMessage errorMessage =
        ResourceErrorMessage.builder()
            .message(exception.getMessage())
            .httpStatus(HttpStatus.CONFLICT)
            .build();
    return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(value = {ReservationOverlapException.class})
  public ResponseEntity<Object> reservationOverlapException(ReservationOverlapException exception) {
    ResourceErrorMessage errorMessage =
        ResourceErrorMessage.builder()
            .message(exception.getMessage())
            .httpStatus(HttpStatus.CONFLICT)
            .build();
    return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
  }
  @ExceptionHandler(value = {ResourceUpdateExeption.class})
  public ResponseEntity<Object> resourceUpdateException(ResourceUpdateExeption exception) {
    ResourceErrorMessage errorMessage =
        ResourceErrorMessage.builder()
            .message(exception.getMessage())
            .httpStatus(HttpStatus.CONFLICT)
            .build();
    return new ResponseEntity<>(errorMessage, HttpStatus.CONFLICT);
  }
}
