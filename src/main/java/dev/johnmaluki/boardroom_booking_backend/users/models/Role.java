package dev.johnmaluki.boardroom_booking_backend.users.models;

import dev.johnmaluki.boardroom_booking_backend.core.models.BaseEntity;
import dev.johnmaluki.boardroom_booking_backend.utils.RoleType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="roles")
public class Role extends BaseEntity {
    @Builder.Default
    @Column(name = "authority")
    @Enumerated(EnumType.STRING)
    private RoleType authority = RoleType.USER;

    @Column(name = "user_id")
    private Long userId;
}
