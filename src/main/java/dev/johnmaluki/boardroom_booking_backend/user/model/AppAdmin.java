package dev.johnmaluki.boardroom_booking_backend.user.model;

import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="administrators")
public class AppAdmin extends BaseEntity {
    @Column(name = "email", unique = true, nullable = false)
    private String email;
}
