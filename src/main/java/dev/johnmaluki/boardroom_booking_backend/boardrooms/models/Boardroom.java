package dev.johnmaluki.boardroom_booking_backend.boardrooms.models;

import dev.johnmaluki.boardroom_booking_backend.core.models.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="boardrooms")
public class Boardroom extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Lob
    @Column(name = "picture")
    private byte[] picture;

    @Column(name = "description", length = 300, nullable = false)
    private String description;

    @Column(name = "centre")
    private String centre;

    @Column(name = "department")
    private String department;

    @Builder.Default
    @Column(name = "internet_enabled", nullable = false)
    private boolean internetEnabled = true;

    @Column(name = "meeting_type_supported")
    private String meetingTypeSupported; // contains comma separated values

    @Column(name = "admin_id")
    private Long adminId;

}
