package dev.johnmaluki.boardroom_booking_backend.core.exception.exception_handler;

import dev.johnmaluki.boardroom_booking_backend.core.exception.DuplicateResourceException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.JwtTokenException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceErrorMessage;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceOwnershipException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
public class ResourceExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Object> resourceNotFoundExceptionHandler(
      ResourceNotFoundException exception) {
    return errorResponse(exception.getMessage(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ResourceOwnershipException.class)
  public ResponseEntity<Object> resourceAccessExceptionHandler(
      ResourceOwnershipException exception) {
    return errorResponse(exception.getMessage(), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(JwtTokenException.class)
  public ResponseEntity<Object> jwtTokenExceptionHandler(JwtTokenException exception) {
    return errorResponse(exception.getMessage(), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<Object> duplicateResourceExceptionHandler(
      DuplicateResourceException exception) {
    return errorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }

  /** Failed login or an unusable refresh token. */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Object> badCredentialsExceptionHandler(BadCredentialsException exception) {
    return errorResponse(exception.getMessage(), HttpStatus.UNAUTHORIZED);
  }

  /**
   * WebFlux raises {@link WebExchangeBindException} where Spring MVC raised {@code
   * MethodArgumentNotValidException}; the field-to-message response body is unchanged.
   */
  @ExceptionHandler(WebExchangeBindException.class)
  public ResponseEntity<Map<String, String>> invalidArgumentExceptionHandler(
      WebExchangeBindException exception) {
    Map<String, String> errorMessage = new HashMap<>();
    exception
        .getBindingResult()
        .getFieldErrors()
        .forEach(error -> errorMessage.put(error.getField(), error.getDefaultMessage()));
    return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
  }

  private ResponseEntity<Object> errorResponse(String message, HttpStatus status) {
    return new ResponseEntity<>(
        ResourceErrorMessage.builder().message(message).httpStatus(status).build(), status);
  }
}
