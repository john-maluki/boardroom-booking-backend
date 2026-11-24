package dev.johnmaluki.boardroom_booking_backend.messaging.rabbit;

/** Exchange, queue and routing key names shared by the publishers and the workers. */
public final class RabbitTopology {
  public static final String NOTIFICATION_EXCHANGE = "boardroom.notifications";
  public static final String NOTIFICATION_QUEUE = "boardroom.notifications.q";
  public static final String NOTIFICATION_ROUTING_KEY = "notification.created";
  public static final String NOTIFICATION_BINDING_PATTERN = "notification.#";
  public static final String NOTIFICATION_DLX = "boardroom.notifications.dlx";
  public static final String NOTIFICATION_DLQ = "boardroom.notifications.dlq";

  public static final String EMAIL_EXCHANGE = "boardroom.email";
  public static final String EMAIL_QUEUE = "boardroom.email.q";
  public static final String EMAIL_ROUTING_KEY = "email.reservation";
  public static final String EMAIL_BINDING_PATTERN = "email.#";
  public static final String EMAIL_DLX = "boardroom.email.dlx";
  public static final String EMAIL_DLQ = "boardroom.email.dlq";

  private RabbitTopology() {}
}
