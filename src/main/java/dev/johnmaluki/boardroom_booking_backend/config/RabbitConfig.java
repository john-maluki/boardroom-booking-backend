package dev.johnmaluki.boardroom_booking_backend.config;

import tools.jackson.databind.json.JsonMapper;
import dev.johnmaluki.boardroom_booking_backend.messaging.rabbit.RabbitTopology;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology for the two things that were previously done inline on the request thread:
 * raising in-app notifications and sending email.
 *
 * <p>Both now leave the request as a message, which is what makes the write paths fast and
 * genuinely non-blocking — JavaMail in particular is a blocking SMTP conversation that has no
 * business happening while a client waits for its {@code 201}.
 *
 * <p>Each work queue is declared with a dead-letter exchange. Combined with
 * {@code default-requeue-rejected=false} and a bounded retry policy in the properties, a message
 * that cannot be processed is retried a few times and then parked on its dead-letter queue for
 * inspection, instead of spinning forever at the head of the queue.
 */
@Configuration
public class RabbitConfig {

  @Bean
  public TopicExchange notificationExchange() {
    return new TopicExchange(RabbitTopology.NOTIFICATION_EXCHANGE, true, false);
  }

  @Bean
  public TopicExchange notificationDeadLetterExchange() {
    return new TopicExchange(RabbitTopology.NOTIFICATION_DLX, true, false);
  }

  @Bean
  public Queue notificationQueue() {
    return QueueBuilder.durable(RabbitTopology.NOTIFICATION_QUEUE)
        .deadLetterExchange(RabbitTopology.NOTIFICATION_DLX)
        .build();
  }

  @Bean
  public Queue notificationDeadLetterQueue() {
    return QueueBuilder.durable(RabbitTopology.NOTIFICATION_DLQ).build();
  }

  @Bean
  public Binding notificationBinding() {
    return BindingBuilder.bind(notificationQueue())
        .to(notificationExchange())
        .with(RabbitTopology.NOTIFICATION_BINDING_PATTERN);
  }

  @Bean
  public Binding notificationDeadLetterBinding() {
    return BindingBuilder.bind(notificationDeadLetterQueue())
        .to(notificationDeadLetterExchange())
        .with("#");
  }

  @Bean
  public TopicExchange emailExchange() {
    return new TopicExchange(RabbitTopology.EMAIL_EXCHANGE, true, false);
  }

  @Bean
  public TopicExchange emailDeadLetterExchange() {
    return new TopicExchange(RabbitTopology.EMAIL_DLX, true, false);
  }

  @Bean
  public Queue emailQueue() {
    return QueueBuilder.durable(RabbitTopology.EMAIL_QUEUE)
        .deadLetterExchange(RabbitTopology.EMAIL_DLX)
        .build();
  }

  @Bean
  public Queue emailDeadLetterQueue() {
    return QueueBuilder.durable(RabbitTopology.EMAIL_DLQ).build();
  }

  @Bean
  public Binding emailBinding() {
    return BindingBuilder.bind(emailQueue())
        .to(emailExchange())
        .with(RabbitTopology.EMAIL_BINDING_PATTERN);
  }

  @Bean
  public Binding emailDeadLetterBinding() {
    return BindingBuilder.bind(emailDeadLetterQueue())
        .to(emailDeadLetterExchange())
        .with("#");
  }

  /**
   * JSON on the wire rather than Java serialisation, so the payload records can evolve and the
   * queues stay readable in the management UI.
   *
   * <p>Spring Boot 4 ships Jackson 3, so this is {@code JacksonJsonMessageConverter} over a {@code
   * JsonMapper}; the Jackson 2 {@code Jackson2JsonMessageConverter} still exists in spring-amqp but
   * belongs to the older binding. The application's configured mapper is reused so the records
   * serialise the same way here as they do over HTTP.
   */
  @Bean
  public MessageConverter rabbitMessageConverter(JsonMapper jsonMapper) {
    return new JacksonJsonMessageConverter(jsonMapper);
  }

  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory, MessageConverter rabbitMessageConverter) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(rabbitMessageConverter);
    return template;
  }
}
