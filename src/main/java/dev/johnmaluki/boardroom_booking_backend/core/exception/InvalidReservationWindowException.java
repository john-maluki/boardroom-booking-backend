package dev.johnmaluki.boardroom_booking_backend.core.exception;

/** A reservation's start/end times don't form a valid window (e.g. end not after start). */
public class InvalidReservationWindowException extends RuntimeException {
  public InvalidReservationWindowException(String message) {
    super(message);
  }
}
