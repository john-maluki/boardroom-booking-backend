package dev.johnmaluki.boardroom_booking_backend.messaging.email;

import java.util.Map;
import java.util.Set;

public interface EmailService {
    void sendEmailForReservationApproval(String to, String subject, Map<String, Object> templateModel);
    void sendEmailToAttendeesPlusCreator(Set<String> attendees, String subject, Map<String, Object> templateModel);
    void notifyUserOfReservationApproval(String to, String subject, Map<String, Object> templateModel);
    void notifyAdministratorsOfReservationApproval(Set<String> adminEmails, String subject, Map<String, Object> templateModel);
    void sendNotificationEmailOfReservationUpdate(Set<String> toEmails, String subject, Map<String, Object> templateModel);
}
