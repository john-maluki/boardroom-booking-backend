package dev.johnmaluki.boardroom_booking_backend.user.service;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserServiceUtil {
  /**
   * Every system user holding the ADMIN role.
   *
   * @return users with the admin role
   */
  Flux<AppUser> getAllSystemAdministrators();

  /**
   * One user's email address, or empty when the user does not exist.
   *
   * @param userId the user to look up
   */
  Mono<String> findEmailByUserId(long userId);
}
