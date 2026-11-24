package dev.johnmaluki.boardroom_booking_backend.core.cache;

import tools.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Read-through cache over reactive Redis.
 *
 * <p>Spring's {@code @Cacheable} is deliberately not used here: it caches the {@code Mono} or
 * {@code Flux} object itself rather than the value it will eventually emit, which for a cold
 * publisher means caching a recipe instead of a result. So the read-through is explicit, the value
 * is stored as JSON, and the caller names the type it wants back.
 *
 * <p>Redis is treated as an accelerator, never a dependency. Any failure talking to it — it is
 * down, the payload no longer deserialises after a DTO change — is logged and falls through to the
 * loader, so a cache problem degrades latency instead of breaking the endpoint.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReactiveCacheService {
  private final ReactiveStringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  /** Caches a single value, or returns the loader's value if Redis cannot serve it. */
  public <T> Mono<T> cache(String key, Duration ttl, Class<T> type, Mono<T> loader) {
    return redisTemplate
        .opsForValue()
        .get(key)
        .flatMap(json -> read(json, type, key))
        .switchIfEmpty(Mono.defer(() -> loader.flatMap(value -> write(key, ttl, value))))
        .onErrorResume(error -> fallback(key, error, loader));
  }

  /**
   * Caches a whole list under one key. The list is stored as a single JSON document rather than a
   * Redis list so that it is replaced atomically and expires as a unit.
   */
  public <T> Flux<T> cacheList(String key, Duration ttl, Class<T> type, Flux<T> loader) {
    return redisTemplate
        .opsForValue()
        .get(key)
        .flatMap(json -> readList(json, type, key))
        .switchIfEmpty(Mono.defer(() -> loader.collectList().flatMap(list -> write(key, ttl, list))))
        .onErrorResume(error -> fallback(key, error, loader.collectList()))
        .flatMapMany(Flux::fromIterable);
  }

  /** Drops specific keys. Failures are swallowed: a stale entry is better than a failed write. */
  public Mono<Void> evict(String... keys) {
    if (keys.length == 0) {
      return Mono.empty();
    }
    return redisTemplate
        .delete(Flux.fromArray(keys))
        .doOnError(error -> log.warn("Could not evict cache keys {}: {}", keys, error.getMessage()))
        .onErrorReturn(0L)
        .then();
  }

  /** Drops every key matching a glob, used to invalidate a whole aggregate after a write. */
  public Mono<Void> evictByPattern(String pattern) {
    return redisTemplate
        .delete(redisTemplate.scan(org.springframework.data.redis.core.ScanOptions.scanOptions()
            .match(pattern)
            .count(500)
            .build()))
        .doOnError(
            error ->
                log.warn("Could not evict cache pattern {}: {}", pattern, error.getMessage()))
        .onErrorReturn(0L)
        .then();
  }

  private <T> Mono<T> read(String json, Class<T> type, String key) {
    try {
      return Mono.justOrEmpty(objectMapper.readValue(json, type));
    } catch (Exception e) {
      log.warn("Discarding unreadable cache entry {}: {}", key, e.getMessage());
      return Mono.empty();
    }
  }

  private <T> Mono<List<T>> readList(String json, Class<T> type, String key) {
    try {
      List<T> values =
          objectMapper.readValue(
              json,
              objectMapper.getTypeFactory().constructCollectionType(List.class, type));
      return Mono.just(values);
    } catch (Exception e) {
      log.warn("Discarding unreadable cache entry {}: {}", key, e.getMessage());
      return Mono.empty();
    }
  }

  private <V> Mono<V> write(String key, Duration ttl, V value) {
    String json;
    try {
      json = objectMapper.writeValueAsString(value);
    } catch (Exception e) {
      log.warn("Could not serialise value for cache key {}: {}", key, e.getMessage());
      return Mono.just(value);
    }
    return redisTemplate
        .opsForValue()
        .set(key, json, ttl)
        .doOnError(error -> log.warn("Could not cache key {}: {}", key, error.getMessage()))
        .onErrorReturn(false)
        .thenReturn(value);
  }

  private <V> Mono<V> fallback(String key, Throwable error, Mono<V> loader) {
    log.warn("Cache unavailable for key {} ({}); loading from source", key, error.getMessage());
    return loader;
  }

}
