package dev.johnmaluki.boardroom_booking_backend.core.exception;

import lombok.Builder;
import org.springframework.http.HttpStatus;

/**
 * The shape every error response takes.
 *
 * <p>Deliberately just these two fields. An earlier version also carried the triggering {@code
 * Throwable} — but every custom exception type gets here with a plain, already-safe {@code
 * message}, and serializing a raw {@code Throwable} with Jackson walks its bean-style getters,
 * which includes {@code getStackTrace()}: the moment any call site ever passes a real cause, the
 * client would receive the full server-side stack trace in the response body. Nothing here ever
 * needs to forward that detail to a client; what's worth keeping is logged server-side instead —
 * see {@code GlobalExceptionHandler}'s catch-all.
 */
@Builder
public record ResourceErrorMessage(String message, HttpStatus httpStatus) {}
