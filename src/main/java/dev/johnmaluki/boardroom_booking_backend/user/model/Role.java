package dev.johnmaluki.boardroom_booking_backend.user.model;

import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
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

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;
}
