package dev.johnmaluki.boardroom_booking_backend.user.model;

import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * R2DBC has no notion of associations, so the collections that used to hang off this entity
 * (boardrooms, reservations) are now queried through their own repositories by foreign key. The
 * role is likewise a separate row, joined in where it is needed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Table("users")
public class AppUser extends BaseEntity {
  @Column("username")
  private String username;

  @Column("full_name")
  private String fullName;

  @Column("department")
  private String department;

  @Column("email")
  private String email;

  @Builder.Default
  @Column("enabled")
  private Boolean enabled = true;

  @Builder.Default
  @Column("activated")
  private Boolean activated = true;

  @Column("time_zone")
  private String timeZone;
}
