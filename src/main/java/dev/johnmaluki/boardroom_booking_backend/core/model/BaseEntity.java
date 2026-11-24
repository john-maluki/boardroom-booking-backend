package dev.johnmaluki.boardroom_booking_backend.core.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

/**
 * Root of every persistent aggregate. A {@code null} id is what tells Spring Data R2DBC that the
 * row is new, so the id must stay {@code null} until the database assigns one.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public abstract class BaseEntity extends AuditableAndDeletable<Long> {
  @Id
  @Column("id")
  private Long id;

  @Column("tag")
  private String tag;
}
