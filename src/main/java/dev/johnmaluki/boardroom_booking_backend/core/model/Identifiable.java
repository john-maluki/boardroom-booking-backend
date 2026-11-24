package dev.johnmaluki.boardroom_booking_backend.core.model;

import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;

/**
 * An abstract base class model that provides a universally unique identifier for each instance of
 * any model that inherits from it.
 *
 * <p>It features an identifier field for external references or additional identification needs.
 * occurred_at tracks the occurrence of a specific event.
 *
 * <p>The identifier is populated by {@link
 * dev.johnmaluki.boardroom_booking_backend.core.callback.BaseEntityCallback} before the row is
 * converted for an insert, which is the R2DBC equivalent of JPA's {@code @PrePersist}.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@SuperBuilder
public abstract class Identifiable {
  @Column("identifier")
  private String identifier;

  @Builder.Default
  @Column("occurred_at")
  private LocalDateTime occurredAt = LocalDateTime.now();
}
