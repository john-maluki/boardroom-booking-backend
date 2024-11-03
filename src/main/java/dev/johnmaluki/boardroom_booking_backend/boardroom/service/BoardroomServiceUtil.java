package dev.johnmaluki.boardroom_booking_backend.boardroom.service;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationEventDateDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;

public interface BoardroomServiceUtil {
  Boardroom findBoardroomById(long boardroomId);

  boolean checkAnyReservationOverlap(
     Long reservationId, Long boardroomId, ReservationEventDateDto reservationEventDateDto);
  void checkAnyReservationOverlap(
      Reservation reservation);
}
