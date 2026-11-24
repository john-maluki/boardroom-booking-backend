package dev.johnmaluki.boardroom_booking_backend.user.model;

import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import lombok.AllArgsConstructor;
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
@Table("administrators")
public class AppAdmin extends BaseEntity {
  @Column("email")
  private String email;
}
