package dev.johnmaluki.boardroom_booking_backend.core.exception.exception_handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceErrorMessage;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebInputException;

/**
 * Exercises the two safety-net handlers added alongside the application's own business
 * exceptions: a database constraint hit nowhere else, and a WebFlux input-decoding failure.
 *
 * <p>Each assertion also checks the response body never contains more than {@code message} and
 * {@code httpStatus} — {@link ResourceErrorMessage} dropped a {@code Throwable} field for exactly
 * this reason, and the point of these tests is to make sure a raw database or decoder message
 * (which can carry column names, table names, or fragments of a malformed request body) never
 * reaches the client, even though it is deliberately still logged server-side.
 */
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void dataIntegrityViolationBecomesAConflictWithAGenericMessage() {
    DataIntegrityViolationException dbError =
        new DataIntegrityViolationException(
            "duplicate key value violates unique constraint \"users_email_key\" detail: Key"
                + " (email)=(someone@kemri.go.ke) already exists.");

    ResponseEntity<Object> response = handler.dataIntegrityViolationExceptionHandler(dbError);

    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    ResourceErrorMessage body = assertInstanceOf(ResourceErrorMessage.class, response.getBody());
    assertEquals(HttpStatus.CONFLICT, body.httpStatus());
    assertFalse(
        body.message().contains("users_email_key"),
        "the response must not leak the real constraint name");
    assertFalse(
        body.message().contains("someone@kemri.go.ke"),
        "the response must not leak the real column value");
  }

  @Test
  void responseStatusExceptionNeverForwardsItsReasonText() {
    // A ServerWebInputException's reason is NOT a framework-sanitised summary — it is simply
    // whatever string the thrower passed in, and WebFlux's own internals build it by embedding
    // the raw decoder exception's message for exactly this exception type. This reason
    // deliberately echoes what a real malformed-body failure looks like, to prove the handler
    // does not trust getReason() either.
    ServerWebInputException decodeFailure =
        new ServerWebInputException("JSON decoding error: Unexpected character ('}' ...)");

    ResponseEntity<Object> response = handler.responseStatusExceptionHandler(decodeFailure);

    ResourceErrorMessage body = assertInstanceOf(ResourceErrorMessage.class, response.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(HttpStatus.BAD_REQUEST, body.httpStatus());
    assertEquals("Bad Request", body.message());
    assertFalse(body.message().contains("Unexpected character"));
  }

  @Test
  void unhandledExceptionNeverForwardsItsOwnMessage() {
    RuntimeException bug = new NullPointerException("boardroom.getAdministrator() was null");

    ResponseEntity<Object> response = handler.unhandledExceptionHandler(bug);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ResourceErrorMessage body = assertInstanceOf(ResourceErrorMessage.class, response.getBody());
    assertFalse(body.message().contains("boardroom.getAdministrator()"));
    assertTrue(body.message().contains("unexpected"));
  }
}
