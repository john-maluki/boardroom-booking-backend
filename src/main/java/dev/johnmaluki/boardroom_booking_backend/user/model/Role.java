package dev.johnmaluki.boardroom_booking_backend.user.model;

import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Table("roles")
public class Role extends BaseEntity {
  @Builder.Default
  @Column("authority")
  private RoleType authority = RoleType.USER;

  @Column("user_id")
  private Long userId;
}
