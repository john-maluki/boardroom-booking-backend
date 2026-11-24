package dev.johnmaluki.boardroom_booking_backend.core.model;

import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;

/**
 * An abstract base class model that provides audit fields for tracking entity creation and updates.
 * It also adds a deleted field to any inheriting model, allowing for soft deletion. Instead of
 * records being removed from the database, the deleted field is used to mark entities as deleted.
 *
 * <p>Fields:
 *
 * <p>created_at(DateTime): Timestamp of when an entity was created
 *
 * <p>updated_at(DateTime): Timestamp of when an entity was updated
 *
 * <p>created_by(Long): The user who created the entity
 *
 * <p>updated_by(Long): The user who updated the entity
 *
 * <p>deleted(Boolean): enable soft deletions
 *
 * <p>The audit annotations are driven by {@code @EnableR2dbcAuditing} together with the {@link
 * org.springframework.data.domain.ReactiveAuditorAware} bean, which resolves the current user from
 * the Reactor context rather than from a thread local.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public abstract class AuditableAndDeletable<T> extends Archival {
  @CreatedDate
  @Column("created_at")
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column("updated_at")
  private LocalDateTime updatedAt;

  @CreatedBy
  @Column("created_by")
  private T createdBy;

  @LastModifiedBy
  @Column("updated_by")
  private T updatedBy;

  @Builder.Default
  @Column("deleted")
  private Boolean deleted = false;
}
