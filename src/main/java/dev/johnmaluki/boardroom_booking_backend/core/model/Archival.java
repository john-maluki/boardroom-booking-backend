package dev.johnmaluki.boardroom_booking_backend.core.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;

/**
 * An abstract base class model that adds an archived field to any inheriting model, allowing for
 * archiving.
 *
 * <p>Instead of records being removed from the database, the archived field is used to mark
 * entities. This approach preserves the entities for audit or other purposes while effectively
 * hiding them from normal application operations.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public abstract class Archival extends Identifiable {
  @Builder.Default
  @Column("archived")
  private Boolean archived = false;
}
