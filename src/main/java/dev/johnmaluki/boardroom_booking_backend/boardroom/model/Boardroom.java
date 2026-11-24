package dev.johnmaluki.boardroom_booking_backend.boardroom.model;

import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Table("boardrooms")
public class Boardroom extends BaseEntity {
  @Column("name")
  private String name;

  @Column("email")
  private String email;

  @Column("capacity")
  private int capacity;

  @Column("picture")
  private String picture;

  @Column("description")
  private String description;

  @Column("centre")
  private String centre;

  @Column("department")
  private String department;

  @Builder.Default
  @Column("internet_enabled")
  private boolean internetEnabled = true;

  /** Comma separated values. */
  @Builder.Default
  @Column("meeting_type_supported")
  private String meetingTypeSupported = "Physical,Hybrid";

  @Builder.Default
  @Column("locked")
  private boolean locked = false;

  @Column("administrator_id")
  private Long administratorId;

  /** Derived per request from the reservations table; never persisted. */
  @Transient private boolean hasOngoingMeeting;
}
