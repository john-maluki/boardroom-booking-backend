package dev.johnmaluki.boardroom_booking_backend.notification.model;

import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Table("notifications")
public class ApplicationNotification extends BaseEntity {
  @Column("title")
  private String title;

  @Column("message")
  private String message;

  /** Comma separated user ids. */
  @Column("recipient")
  private String recipient;

  @Builder.Default
  @Column("is_read")
  private boolean isRead = false;

  @Column("reservation_id")
  private Long reservationId;
}
