package dev.johnmaluki.boardroom_booking_backend.core.exception.exception_handler;

import dev.johnmaluki.boardroom_booking_backend.core.exception.FileUploadException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.InvalidReservationWindowException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ReservationOverlapException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceErrorMessage;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceUpdateExeption;
import dev.johnmaluki.boardroom_booking_backend.core.exception.UniqueContraintException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Catches the application's own business exceptions, plus two safety nets that apply everywhere
 * this class's siblings don't: any constraint violation that wasn't already turned into a nicer
 * business exception close to where it happened, and — last resort — anything genuinely
 * unanticipated.
 *
 * <p>Neither safety net needs special placement to take effect: Spring resolves {@code
 * @ExceptionHandler} methods across every {@code @RestControllerAdvice} bean by how specific each
 * one's declared exception type is, not by which class declares it or in what order. A handler
 * here for {@code Exception} will never shadow a more specific one declared on {@code
 * ResourceExceptionHandler} or elsewhere in this class.
 *
 * <p>One thing this class structurally cannot catch: an authentication or authorization failure
 * raised inside the Spring Security filter chain (bad token, insufficient role) never reaches
 * {@code @ExceptionHandler} resolution at all — it is intercepted earlier, by {@code
 * ExceptionTranslationWebFilter}. That path's response shape is kept consistent with this one
 * instead via {@code SecurityConfig}'s {@code RestAuthenticationEntryPoint} /
 * {@code RestAccessDeniedHandler}.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(FileUploadException.class)
  public ResponseEntity<Object> fileUploadExceptionHandler(FileUploadException exception) {
    return errorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(UniqueContraintException.class)
  public ResponseEntity<Object> dataIntegrityViolationException(
      UniqueContraintException exception) {
    return errorResponse(exception.getMessage(), HttpStatus.CONFLICT);
  }

  /** 409 with the clashing bookings, so the client can say what the time conflicts with. */
  @ExceptionHandler(ReservationOverlapException.class)
  public ResponseEntity<Object> reservationOverlapException(
      ReservationOverlapException exception) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("message", exception.getMessage());
    body.put("httpStatus", HttpStatus.CONFLICT);
    body.put("conflicts", exception.getConflicts());
    return new ResponseEntity<>(body, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(InvalidReservationWindowException.class)
  public ResponseEntity<Object> invalidReservationWindowException(
      InvalidReservationWindowException exception) {
    return errorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ResourceUpdateExeption.class)
  public ResponseEntity<Object> resourceUpdateException(ResourceUpdateExeption exception) {
    return errorResponse(exception.getMessage(), HttpStatus.CONFLICT);
  }

  /**
   * Safety net for a unique or foreign-key constraint hit anywhere that didn't already translate
   * it locally into a more specific exception (compare {@code
   * BoardroomServiceImpl.createBoardroom}, which maps the same exception into {@link
   * UniqueContraintException} for a nicer message). Without this, a write path that never
   * anticipated the constraint — a username collision during AD login provisioning, say — would
   * fall through uncaught to the generic 500 below instead of a clear 409.
   *
   * <p>The database driver's own message can mention real column and constraint names; it is
   * logged, not returned, so the client gets a safe, generic explanation instead.
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Object> dataIntegrityViolationExceptionHandler(
      DataIntegrityViolationException exception) {
    log.warn("Rejected write that violated a database constraint: {}", exception.getMessage());
    return errorResponse(
        "The request conflicts with an existing record.", HttpStatus.CONFLICT);
  }

  /**
   * Safety net for WebFlux's own input-handling failures — a request body that isn't valid JSON,
   * or a path variable that doesn't match its declared type (such as a non-numeric id) — which
   * fail before an {@code @Valid} body is ever bound, so {@code WebExchangeBindException} never
   * gets a chance to fire.
   *
   * <p>Neither {@code getReason()} nor {@code getMessage()} is forwarded: {@code reason} looks
   * like a safe, framework-supplied summary, but it is just whatever string the thrower passed
   * in, and WebFlux's own internals build it by embedding the underlying decoder exception's
   * message for exactly this exception type — so a deliberately malformed body could still come
   * back verbatim in the response. {@code status.getReasonPhrase()} ("Bad Request") is the one
   * part that is always a fixed, known string; the real reason is logged instead.
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Object> responseStatusExceptionHandler(ResponseStatusException exception) {
    HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
    log.debug("Rejected request: {}", exception.getReason(), exception);
    return errorResponse(status.getReasonPhrase(), status);
  }

  /**
   * Last resort. Anything reaching here is, by definition, a failure mode nobody wrote a specific
   * handler for — a {@code NullPointerException} from a bug, an {@code IllegalStateException}
   * from a misconfigured environment variable, and so on. The full exception goes to the log, at
   * {@code ERROR}, because that is where whoever is on call needs to see it; the client gets a
   * fixed, generic message, because the exception's own message was never written with a client
   * audience in mind and may describe internals (a SQL fragment, a file path, a class name) that
   * have no business leaving the server.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> unhandledExceptionHandler(Exception exception) {
    log.error("Unhandled exception reached the top of the request", exception);
    return errorResponse(
        "An unexpected error occurred. Please try again or contact support if the problem"
            + " persists.",
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<Object> errorResponse(String message, HttpStatus status) {
    return new ResponseEntity<>(
        ResourceErrorMessage.builder().message(message).httpStatus(status).build(), status);
  }
}
