package dev.johnmaluki.boardroom_booking_backend.boardroom.model;

import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import dev.johnmaluki.boardroom_booking_backend.util.CommunicationLineType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Table("boardroom_contact")
public class BoardroomContact extends BaseEntity {
  @Column("boardroom_id")
  private Long boardroomId;

  @Builder.Default
  @Column("communication_method")
  private CommunicationLineType communicationMethod = CommunicationLineType.PHONE_EXTENSION;

  @Column("contact")
  private String contact;
}
