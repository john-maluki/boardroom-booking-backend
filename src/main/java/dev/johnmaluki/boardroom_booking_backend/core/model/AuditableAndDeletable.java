package dev.johnmaluki.boardroom_booking_backend.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * An abstract base class model that provides audit fields for tracking entity creation, updates and
 * the occurrence of a specific event. It also adds a deleted field to any inheriting model,
 * allowing for soft deletion. Instead of records being removed from the database, the deleted field
 * is used to mark entities as deleted. This approach preserves the deleted entities for audit or
 * other purposes while effectively hiding them from normal application operations. <br>
 * <br>
 * Fields:<br>
 *
 * <p>created_at(DateTime): Timestamp of when an entity was created
 *
 * <p>updated_at(DateTime): Timestamp of when an entity was updated
 *
 * <p>created_by(UUID): The user who created the entity
 *
 * <p>updated_by(UUID): The user who updated the entity
 *
 * <p>deleted(Boolean): enable soft deletions
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableAndDeletable<T> extends Archival {
  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @CreatedBy
  @Column(name = "created_by")
  private T createdBy;

  @LastModifiedBy
  @Column(name = "updated_by")
  private T updatedBy;

  @Builder.Default private Boolean deleted = false;
}
