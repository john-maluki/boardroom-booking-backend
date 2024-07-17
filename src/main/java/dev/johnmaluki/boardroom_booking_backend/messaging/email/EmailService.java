package dev.johnmaluki.boardroom_booking_backend.messaging.email;

import jakarta.mail.MessagingException;

import java.util.Map;

public interface EmailService {
    void sendEmailForReservationApproval(String to, String subject, Map<String, Object> templateModel);
}
