package dev.johnmaluki.boardroom_booking_backend.equipment.model;

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
@Table("equipments")
public class Equipment extends BaseEntity {
  @Column("title")
  private String title;

  @Column("description")
  private String description;

  @Column("picture")
  private String picture;

  @Column("video_url")
  private String videoUrl;

  @Builder.Default
  @Column("disposed")
  private boolean disposed = false;

  @Column("model_number")
  private String modelNumber;

  @Column("brand")
  private String brand;

  @Column("boardroom_id")
  private Long boardroomId;
}
