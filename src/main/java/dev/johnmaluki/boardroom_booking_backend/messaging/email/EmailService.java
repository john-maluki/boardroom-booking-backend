package dev.johnmaluki.boardroom_booking_backend.messaging.email;

import java.util.List;
import java.util.Map;

public interface EmailService {
    void sendEmailForReservationApproval(String to, String subject, Map<String, Object> templateModel);
    void sendEmailToAllAttendees(List<String> attendees, String subject, Map<String, Object> templateModel);
    void notifyUserOfReservationApproval(String to, String subject, Map<String, Object> templateModel);
    void notifyAdministratorsOfReservationApproval(List<String> adminEmails, String subject, Map<String, Object> templateModel);
}
