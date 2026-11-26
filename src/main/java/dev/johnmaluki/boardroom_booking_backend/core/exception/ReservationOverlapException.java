package dev.johnmaluki.boardroom_booking_backend.core.exception;

import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationConflictDto;
import java.util.List;

public class ReservationOverlapException extends RuntimeException {
  private final transient List<ReservationConflictDto> conflicts;

  public ReservationOverlapException(String message) {
    this(message, List.of());
  }

  public ReservationOverlapException(String message, List<ReservationConflictDto> conflicts) {
    super(message);
    this.conflicts = conflicts == null ? List.of() : conflicts;
  }

  /** The bookings the requested window clashes with (may be empty). */
  public List<ReservationConflictDto> getConflicts() {
    return conflicts;
  }
}
