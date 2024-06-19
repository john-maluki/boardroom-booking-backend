package dev.johnmaluki.boardroom_booking_backend.users.models;

import dev.johnmaluki.boardroom_booking_backend.core.models.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="users")
public class User extends BaseEntity {
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "department")
    private String department;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Builder.Default
    @Column(name = "enabled")
    private boolean enabled = true;

    @Builder.Default
    @Column(name = "activated")
    private boolean activated = true;

    @Column(name = "time_zone", nullable = false)
    private String timeZone; // Add this field to store the user's time zone

}
