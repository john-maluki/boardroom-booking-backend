package dev.johnmaluki.boardroom_booking_backend.boardroom.service;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationEventDateDto;

public interface BoardroomServiceUtil {
  Boardroom findBoardroomById(long boardroomId);

  boolean checkAnyReservationOverlap(
      long boardroomId, ReservationEventDateDto reservationEventDateDto);
}
