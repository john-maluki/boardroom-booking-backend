package dev.johnmaluki.boardroom_booking_backend.notification.repository;

import dev.johnmaluki.boardroom_booking_backend.notification.view.NotificationView;
import io.r2dbc.spi.Readable;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Read side for notifications, joined through the reservation to the boardroom and the booker.
 *
 * <p>The recipient list lives in the row as a comma separated string of user ids, which is how the
 * JPA version stored it. Filtering a non-admin's feed therefore happens in SQL with a {@code LIKE}
 * over that list padded with commas on both ends, so that id {@code 1} does not match {@code 12}.
 */
@Repository
@RequiredArgsConstructor
public class NotificationQueryRepository {
  private final DatabaseClient databaseClient;

  private static final String SELECT_VIEW =
      """
      SELECT n.id, n.title, n.message, n.recipient, n.is_read, n.created_at,
             n.reservation_id,
             b.name AS boardroom_name,
             u.full_name AS created_by_full_name,
             u.email AS created_by_email
      FROM notifications n
      JOIN reservations r ON r.id = n.reservation_id
      JOIN boardrooms b ON b.id = r.boardroom_id
      JOIN users u ON u.id = r.user_id
      """;

  /**
   * Recent, still-active notifications. Pass {@code null} as the user id for an administrator, who
   * sees every notification; any other value restricts the feed to notifications addressed to that
   * user.
   */
  public Flux<NotificationView> findRecentActive(LocalDateTime since, Long userId) {
    DatabaseClient.GenericExecuteSpec spec =
        databaseClient
            .sql(
                SELECT_VIEW
                    + """
                    WHERE n.archived = false AND n.deleted = false
                      AND n.created_at >= :since
                      AND (:userId IS NULL
                           OR ',' || n.recipient || ',' LIKE '%,' || :userId || ',%')
                    ORDER BY n.created_at DESC
                    """)
            .bind("since", since);
    spec =
        userId == null
            ? spec.bindNull("userId", String.class)
            : spec.bind("userId", String.valueOf(userId));
    return spec.map(NotificationQueryRepository::mapView).all();
  }

  public Mono<NotificationView> findViewById(long notificationId) {
    return databaseClient
        .sql(SELECT_VIEW + " WHERE n.id = :id")
        .bind("id", notificationId)
        .map(NotificationQueryRepository::mapView)
        .one();
  }

  private static NotificationView mapView(Readable row) {
    return new NotificationView(
        row.get("id", Long.class),
        row.get("title", String.class),
        row.get("message", String.class),
        row.get("recipient", String.class),
        Boolean.TRUE.equals(row.get("is_read", Boolean.class)),
        row.get("created_at", LocalDateTime.class),
        row.get("reservation_id", Long.class),
        row.get("boardroom_name", String.class),
        row.get("created_by_full_name", String.class),
        row.get("created_by_email", String.class));
  }
}
