package dev.johnmaluki.boardroom_booking_backend.user.repository;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AppUserRepository extends ReactiveCrudRepository<AppUser, Long> {
  Mono<AppUser> findByUsername(String username);

  Mono<AppUser> findByIdAndArchivedFalseAndDeletedFalse(long userId);

  @Query(
      """
      SELECT u.* FROM users u
      JOIN roles r ON r.user_id = u.id
      WHERE r.authority = 'ADMIN' AND u.deleted = false AND u.archived = false
      """)
  Flux<AppUser> findApplicationAdministrators();
}
