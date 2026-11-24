package dev.johnmaluki.boardroom_booking_backend.boardroom.model;

import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Table("locked_boardrooms")
public class LockedRoom extends BaseEntity {
  @Column("boardroom_id")
  private Long boardroomId;

  @Builder.Default
  @Column("locked")
  private boolean locked = true;

  @Column("given_reason")
  private String givenReason;
}
