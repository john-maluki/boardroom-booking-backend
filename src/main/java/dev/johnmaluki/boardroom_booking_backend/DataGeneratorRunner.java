package dev.johnmaluki.boardroom_booking_backend;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppAdmin;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppAdminRepository;
import dev.johnmaluki.boardroom_booking_backend.util.UserUtil;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Seeds the administrator allow-list from {@code DEFAULT_SYSTEM_ADMIN_EMAILS}.
 *
 * <p>Runs once at startup, so the reactive chain is driven with {@code block()} — the application
 * is not yet serving traffic and startup must not proceed until the seed is in place.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataGeneratorRunner implements ApplicationRunner {
  private static final Duration SEED_TIMEOUT = Duration.ofSeconds(30);

  private final AppAdminRepository appAdminRepository;
  private final UserUtil userUtil;

  @Override
  public void run(ApplicationArguments args) {
    List<String> emails = userUtil.getDefaultSystemAdministrators();
    if (emails.isEmpty()) {
      return;
    }
    appAdminRepository
        .findAllByEmailIn(emails)
        .map(AppAdmin::getEmail)
        .collect(java.util.stream.Collectors.toSet())
        .flatMapMany(existing -> saveMissing(emails, existing))
        .then()
        .block(SEED_TIMEOUT);
  }

  private Flux<AppAdmin> saveMissing(List<String> emails, Set<String> existingEmails) {
    List<AppAdmin> toCreate =
        emails.stream()
            .filter(email -> !existingEmails.contains(email))
            .map(AppAdmin::new)
            .toList();
    if (toCreate.isEmpty()) {
      return Flux.empty();
    }
    log.info("Seeding {} default system administrator(s)", toCreate.size());
    return appAdminRepository.saveAll(toCreate);
  }
}
