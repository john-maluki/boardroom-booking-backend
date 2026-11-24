package dev.johnmaluki.boardroom_booking_backend.reservation.repository;

import dev.johnmaluki.boardroom_booking_backend.boardroom.view.BoardroomMeetingStatus;
import dev.johnmaluki.boardroom_booking_backend.reservation.view.ReservationView;
import dev.johnmaluki.boardroom_booking_backend.util.ApprovalStatus;
import dev.johnmaluki.boardroom_booking_backend.util.MeetingType;
import io.r2dbc.spi.Readable;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Hand-written read side for reservations.
 *
 * <p>Every list endpoint needs the boardroom name and the booker's name and email alongside the
 * reservation itself. Expressing those as explicit joins mapped by hand keeps the SQL visible and
 * avoids both the N+1 fan-out and any guesswork about how a projection gets bound.
 *
 * <p>{@code userId} is nullable in each query: pass {@code null} for an administrator, who sees
 * every row, or a user id to restrict the result to that user's own bookings. Doing that filter in
 * SQL means a regular user never pulls the whole table across the wire.
 */
@Repository
@RequiredArgsConstructor
public class ReservationQueryRepository {
  private final DatabaseClient databaseClient;

  private static final String SELECT_VIEW =
      """
      SELECT r.id, r.tag, r.start_date_time, r.end_date_time, r.approval_status,
             r.cancellation_message, r.meeting_title, r.meeting_description, r.meeting_type,
             r.attendees, r.ict_support_required, r.is_urgent_meeting, r.record_meeting,
             r.meeting_link, r.created_at,
             b.id AS boardroom_id, b.name AS boardroom_name, b.email AS boardroom_email,
             b.administrator_id AS boardroom_administrator_id,
             u.id AS user_id, u.full_name AS user_full_name, u.email AS user_email
      FROM reservations r
      JOIN boardrooms b ON b.id = r.boardroom_id
      JOIN users u ON u.id = r.user_id
      """;

  public Flux<ReservationView> findAllActive(Long userId) {
    return views(
        SELECT_VIEW
            + """
            WHERE r.archived = false AND r.deleted = false
              AND (:userId IS NULL OR r.user_id = :userId)
            ORDER BY r.start_date_time DESC
            """,
        spec -> bindUser(spec, userId));
  }

  public Flux<ReservationView> findUpcoming(Long userId, LocalDateTime now) {
    return views(
        SELECT_VIEW
            + """
            WHERE r.archived = false AND r.deleted = false
              AND r.start_date_time > :now
              AND (:userId IS NULL OR r.user_id = :userId)
            ORDER BY r.start_date_time DESC
            """,
        spec -> bindUser(spec.bind("now", now), userId));
  }

  public Flux<ReservationView> findLiveMeetings(Long userId, LocalDateTime now) {
    return views(
        SELECT_VIEW
            + """
            WHERE r.archived = false AND r.deleted = false
              AND r.start_date_time < :now AND r.end_date_time > :now
              AND (:userId IS NULL OR r.user_id = :userId)
            ORDER BY r.start_date_time DESC
            """,
        spec -> bindUser(spec.bind("now", now), userId));
  }

  public Flux<ReservationView> findArchived(Long userId) {
    return views(
        SELECT_VIEW
            + """
            WHERE r.archived = true AND r.deleted = false
              AND (:userId IS NULL OR r.user_id = :userId)
            ORDER BY r.start_date_time DESC
            """,
        spec -> bindUser(spec, userId));
  }

  public Flux<ReservationView> findActiveByBoardroom(long boardroomId, Long userId) {
    return views(
        SELECT_VIEW
            + """
            WHERE r.boardroom_id = :boardroomId
              AND r.archived = false AND r.deleted = false
              AND (:userId IS NULL OR r.user_id = :userId)
            ORDER BY r.created_at DESC
            """,
        spec -> bindUser(spec.bind("boardroomId", boardroomId), userId));
  }

  public Flux<ReservationView> findArchivedByBoardroom(long boardroomId, Long userId) {
    return views(
        SELECT_VIEW
            + """
            WHERE r.boardroom_id = :boardroomId
              AND r.archived = true AND r.deleted = false
              AND (:userId IS NULL OR r.user_id = :userId)
            ORDER BY r.created_at DESC
            """,
        spec -> bindUser(spec.bind("boardroomId", boardroomId), userId));
  }

  public Mono<ReservationView> findViewById(long reservationId) {
    return databaseClient
        .sql(SELECT_VIEW + " WHERE r.id = :id AND r.archived = false AND r.deleted = false")
        .bind("id", reservationId)
        .map(ReservationQueryRepository::mapView)
        .one();
  }

  /** One row per boardroom that currently has a meeting running. */
  public Flux<BoardroomMeetingStatus> findBoardroomOngoingMeetingStatus(LocalDateTime now) {
    return databaseClient
        .sql(
            """
            SELECT r.boardroom_id, count(r.id) > 0 AS has_ongoing_meeting
            FROM reservations r
            WHERE :now BETWEEN r.start_date_time AND r.end_date_time
              AND r.archived = false AND r.deleted = false
            GROUP BY r.boardroom_id
            """)
        .bind("now", now)
        .map(
            row ->
                new BoardroomMeetingStatus(
                    row.get("boardroom_id", Long.class),
                    Boolean.TRUE.equals(row.get("has_ongoing_meeting", Boolean.class))))
        .all();
  }

  private Flux<ReservationView> views(
      String sql,
      java.util.function.UnaryOperator<DatabaseClient.GenericExecuteSpec> binder) {
    return binder.apply(databaseClient.sql(sql)).map(ReservationQueryRepository::mapView).all();
  }

  /**
   * Postgres needs a declared type for a null parameter, so a missing user filter is bound as a
   * typed null rather than simply omitted.
   */
  private static DatabaseClient.GenericExecuteSpec bindUser(
      DatabaseClient.GenericExecuteSpec spec, Long userId) {
    return userId == null ? spec.bindNull("userId", Long.class) : spec.bind("userId", userId);
  }

  private static ReservationView mapView(Readable row) {
    return new ReservationView(
        row.get("id", Long.class),
        row.get("tag", String.class),
        row.get("start_date_time", LocalDateTime.class),
        row.get("end_date_time", LocalDateTime.class),
        enumValue(row.get("approval_status", String.class), ApprovalStatus.class),
        row.get("cancellation_message", String.class),
        row.get("meeting_title", String.class),
        row.get("meeting_description", String.class),
        enumValue(row.get("meeting_type", String.class), MeetingType.class),
        row.get("attendees", String.class),
        Boolean.TRUE.equals(row.get("ict_support_required", Boolean.class)),
        Boolean.TRUE.equals(row.get("is_urgent_meeting", Boolean.class)),
        Boolean.TRUE.equals(row.get("record_meeting", Boolean.class)),
        row.get("meeting_link", String.class),
        row.get("created_at", LocalDateTime.class),
        row.get("boardroom_id", Long.class),
        row.get("boardroom_name", String.class),
        row.get("boardroom_email", String.class),
        row.get("boardroom_administrator_id", Long.class),
        row.get("user_id", Long.class),
        row.get("user_full_name", String.class),
        row.get("user_email", String.class));
  }

  private static <E extends Enum<E>> E enumValue(String value, Class<E> type) {
    return value == null ? null : Enum.valueOf(type, value);
  }
}
