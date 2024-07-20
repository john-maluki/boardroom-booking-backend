package dev.johnmaluki.boardroom_booking_backend.messaging.email;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService{
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final Dotenv dotenv;

    @Async
    @Override
    public void sendEmailForReservationApproval(String to, String subject, Map<String, Object> templateModel) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            Context context = new Context();
            context.setVariables(templateModel);
            String htmlBody = this.templateEngine.process("emailReservationTemplate.html", context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom(dotenv.get("DEFAULT_MAIL"));
            // Attach images
            helper.addInline("log", new ClassPathResource("static/img/kemri-logo.png"));
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.info("Sending email exception: {}", e.getMessage());
        }
    }

    @Async
    @Override
    public void sendEmailToAllAttendees(List<String> attendees, String subject, Map<String, Object> templateModel) {
        attendees.forEach(
                email -> System.out.println("Sending email to: " + email)
        );
    }

    @Override
    public void notifyUserOfReservationApproval(String to, String subject, Map<String, Object> templateModel) {
        System.out.println("User notified");
    }

    @Async
    @Override
    public void notifyAdministratorsOfReservationApproval(List<String> adminEmails, String subject, Map<String, Object> templateModel) {
        for (String email : adminEmails) {
            this.notifyAdmin(email, subject, templateModel);
        }
    }

    private void notifyAdmin(String to, String subject, Map<String, Object> templateModel) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            Context context = new Context();
            context.setVariables(templateModel);
            String htmlBody = this.templateEngine.process("notifyAdminReservationApproval.html", context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom(dotenv.get("DEFAULT_MAIL"));
            // Attach images
            helper.addInline("log", new ClassPathResource("static/img/kemri-logo.png"));
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.info("Sending email exception: {}", e.getMessage());
        }
    }

}
