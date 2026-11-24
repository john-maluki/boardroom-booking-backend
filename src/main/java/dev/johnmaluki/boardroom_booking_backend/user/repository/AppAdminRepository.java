package dev.johnmaluki.boardroom_booking_backend.user.repository;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppAdmin;
import java.util.List;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AppAdminRepository extends ReactiveCrudRepository<AppAdmin, Long> {
  Mono<Boolean> existsByEmail(String email);

  Flux<AppAdmin> findAllByArchivedFalseAndDeletedFalse();

  Flux<AppAdmin> findAllByEmailIn(List<String> emails);
}
