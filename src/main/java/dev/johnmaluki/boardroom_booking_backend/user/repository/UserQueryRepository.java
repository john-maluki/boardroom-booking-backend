package dev.johnmaluki.boardroom_booking_backend.user.repository;

import dev.johnmaluki.boardroom_booking_backend.user.view.AppUserView;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import io.r2dbc.spi.Readable;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Read side for users, joined to the single role row that carries their authority.
 *
 * <p>The join is a LEFT join because a user can exist for a moment before their role row is
 * written; the authority then comes back null and callers fall back to {@link RoleType#USER}, which
 * matches how the JPA mapping behaved when {@code role} was still unset.
 */
@Repository
@RequiredArgsConstructor
public class UserQueryRepository {
  private final DatabaseClient databaseClient;

  private static final String SELECT_VIEW =
      """
      SELECT u.id, u.username, u.full_name, u.department, u.email, u.time_zone, u.tag,
             r.authority
      FROM users u
      LEFT JOIN roles r ON r.user_id = u.id
      """;

  public Flux<AppUserView> findAllActive() {
    return databaseClient
        .sql(SELECT_VIEW + " WHERE u.archived = false AND u.deleted = false ORDER BY u.full_name")
        .map(UserQueryRepository::mapView)
        .all();
  }

  public Mono<AppUserView> findActiveById(long userId) {
    return databaseClient
        .sql(SELECT_VIEW + " WHERE u.id = :id AND u.archived = false AND u.deleted = false")
        .bind("id", userId)
        .map(UserQueryRepository::mapView)
        .one();
  }

  public Mono<AppUserView> findActiveByUsername(String username) {
    return databaseClient
        .sql(SELECT_VIEW + " WHERE u.username = :username")
        .bind("username", username)
        .map(UserQueryRepository::mapView)
        .one();
  }

  private static AppUserView mapView(Readable row) {
    String authority = row.get("authority", String.class);
    return new AppUserView(
        row.get("id", Long.class),
        row.get("username", String.class),
        row.get("full_name", String.class),
        row.get("department", String.class),
        row.get("email", String.class),
        row.get("time_zone", String.class),
        row.get("tag", String.class),
        authority == null ? RoleType.USER : RoleType.valueOf(authority));
  }
}
