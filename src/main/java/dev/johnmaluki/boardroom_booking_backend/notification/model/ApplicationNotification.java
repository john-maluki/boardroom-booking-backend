package dev.johnmaluki.boardroom_booking_backend.notification.model;

import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notifications")
public class ApplicationNotification extends BaseEntity {
  @Column(name = "title", nullable = false)
  String title;

  @Column(name = "message", nullable = false, length = 500)
  String message;

  @Column(name = "recipient", nullable = false) // csv column of ids
  String recipient;

  @Default
  @Column(name = "is_read", nullable = false)
  boolean isRead = false;

  @ToString.Exclude
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reservation_id")
  Reservation reservation;
}
