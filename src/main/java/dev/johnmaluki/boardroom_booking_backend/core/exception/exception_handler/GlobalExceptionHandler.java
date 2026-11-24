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

  @ExceptionHandler(FileUploadException.class)
  public ResponseEntity<Object> fileUploadExceptionHandler(FileUploadException exception) {
    return errorResponse(exception.getMessage(), exception.getCause(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UniqueContraintException.class)
  public ResponseEntity<Object> dataIntegrityViolationException(
      UniqueContraintException exception) {
    return errorResponse(exception.getMessage(), null, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(ReservationOverlapException.class)
  public ResponseEntity<Object> reservationOverlapException(
      ReservationOverlapException exception) {
    return errorResponse(exception.getMessage(), null, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(ResourceUpdateExeption.class)
  public ResponseEntity<Object> resourceUpdateException(ResourceUpdateExeption exception) {
    return errorResponse(exception.getMessage(), null, HttpStatus.CONFLICT);
  }

  private ResponseEntity<Object> errorResponse(
      String message, Throwable cause, HttpStatus status) {
    ResourceErrorMessage errorMessage =
        ResourceErrorMessage.builder()
            .message(message)
            .httpStatus(status)
            .throwable(cause)
            .build();
    return new ResponseEntity<>(errorMessage, status);
  }
}
