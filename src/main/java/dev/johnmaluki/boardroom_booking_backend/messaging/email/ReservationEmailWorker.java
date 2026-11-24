package dev.johnmaluki.boardroom_booking_backend.messaging.email;

import dev.johnmaluki.boardroom_booking_backend.core.service.ProfileChecker;
import dev.johnmaluki.boardroom_booking_backend.messaging.event.ReservationEmailEvent;
import dev.johnmaluki.boardroom_booking_backend.messaging.rabbit.RabbitTopology;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Renders and sends the reservation emails taken off the queue.
 *
 * <p>This runs on a Rabbit listener thread, well away from the Netty event loops, which is exactly
 * why the send was moved here: Thymeleaf rendering and the SMTP conversation are both blocking and
 * can take seconds.
 *
 * <p>A send that throws is rethrown so the broker can apply the retry policy and ultimately
 * dead-letter the message. One message may name several recipients; a failure part-way through is
 * retried for the whole message, so a transient SMTP outage can produce a duplicate to the earlier
 * addresses. That is the right trade for notification mail, where a duplicate is harmless and a
 * silently dropped message is not.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEmailWorker {
  private final JavaMailSender mailSender;
  private final SpringTemplateEngine templateEngine;
  private final Dotenv dotenv;
  private final ProfileChecker profileChecker;

  @RabbitListener(queues = RabbitTopology.EMAIL_QUEUE)
  public void onReservationEmail(ReservationEmailEvent event) {
    for (String recipient : event.to()) {
      sendEmail(recipient, event);
    }
  }

  private void sendEmail(String to, ReservationEmailEvent event) {
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    try {
      MimeMessageHelper helper =
          new MimeMessageHelper(
              mimeMessage,
              MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
              StandardCharsets.UTF_8.name());

      Context context = new Context();
      context.setVariable("boardroomName", event.boardroomName());
      context.setVariable("bookedBy", event.bookedBy());
      context.setVariable("meetingType", event.meetingType());
      context.setVariable("meetingTitle", event.meetingTitle());
      context.setVariable("meetingDescription", event.meetingDescription());
      context.setVariable("startDate", event.startDate());
      context.setVariable("endDate", event.endDate());
      context.setVariable("startTime", event.startTime());
      context.setVariable("endTime", event.endTime());
      context.setVariable("meetingLink", event.meetingLink());
      context.setVariable("attendees", event.attendees());
      context.setVariable("detailChange", event.detailChange());

      String htmlBody = templateEngine.process(event.template(), context);

      String fromEmail =
          profileChecker.checkIfAppRunningLocally()
              ? dotenv.get("DEV_DEFAULT_MAIL")
              : dotenv.get("PROD_DEFAULT_MAIL");

      helper.setTo(to);
      helper.setSubject(event.subject());
      helper.setText(htmlBody, true);
      helper.setFrom(fromEmail);
      helper.addInline("log", new ClassPathResource("static/img/kemri-logo.png"));
      mailSender.send(mimeMessage);
      log.debug("Sent '{}' to {}", event.subject(), to);
    } catch (MessagingException e) {
      // Rethrown so the broker retries and eventually dead-letters rather than losing the mail.
      throw new IllegalStateException("Could not send email to " + to, e);
    }
  }
}
