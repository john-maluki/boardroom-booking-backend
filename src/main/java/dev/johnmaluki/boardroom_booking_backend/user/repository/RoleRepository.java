package dev.johnmaluki.boardroom_booking_backend.user.repository;

import dev.johnmaluki.boardroom_booking_backend.user.model.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface RoleRepository extends ReactiveCrudRepository<Role, Long> {
  Mono<Role> findByUserId(long userId);
}
