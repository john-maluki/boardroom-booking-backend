package dev.johnmaluki.boardroom_booking_backend.core.cache;

import java.time.Duration;

/**
 * How long cached reads are allowed to go stale.
 *
 * <p>Writes evict explicitly, so these are a safety net rather than the primary invalidation
 * mechanism — short enough that a change made outside the application still surfaces quickly.
 * Timezones are the exception: the list comes from the JDK and only changes when the runtime does.
 */
public final class CacheTtl {
  public static final Duration REFERENCE_DATA = Duration.ofMinutes(5);
  public static final Duration STATIC_DATA = Duration.ofHours(24);

  private CacheTtl() {}
}
