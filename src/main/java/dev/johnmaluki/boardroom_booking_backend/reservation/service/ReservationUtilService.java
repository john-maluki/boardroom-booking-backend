package dev.johnmaluki.boardroom_booking_backend.reservation.service;

import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ReservationUtilService {
  private boolean checkStringEquality(String t1, String t2) {
    return !t1.trim().equalsIgnoreCase(t2.trim());
  }

  private String prepareString(Reservation reservation) {
    return reservation.getMeetingTitle()
        + reservation.getMeetingDescription()
        + reservation.getMeetingType().name()
        + reservation.isIctSupportRequired()
        + reservation.isUrgentMeeting()
        + reservation.isRecordMeeting()
        + reservation.getAttendees();
  }

  private Set<String> checkAnyNewAttendees(String newCsv1, String currentCsv2) {
    Set<String> emails1 = new HashSet<>(Arrays.asList(newCsv1.split(",")));
    Set<String> emails2 = new HashSet<>(Arrays.asList(currentCsv2.split(",")));
    // Find different emails
    Set<String> uniqueToCsv1 = new HashSet<>(emails1);
    uniqueToCsv1.removeAll(emails2);
    return uniqueToCsv1;
  }

  public boolean checkAnyReservationUpdate(
      Reservation newReservation, Reservation currentReservation) {
    String newString = this.prepareString(newReservation);
    String currentString = this.prepareString(currentReservation);
    return this.checkStringEquality(newString, currentString);
  }

  public Set<String> getNewAttendees(Reservation newReservation, Reservation currentReservation) {
    return this.checkAnyNewAttendees(
        newReservation.getAttendees(), currentReservation.getAttendees());
  }
}
