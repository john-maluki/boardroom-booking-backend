package dev.johnmaluki.boardroom_booking_backend.core.exception;

public class ReservationOverlapException extends RuntimeException{

  public ReservationOverlapException(String message) {
    super(message);
  }
}
