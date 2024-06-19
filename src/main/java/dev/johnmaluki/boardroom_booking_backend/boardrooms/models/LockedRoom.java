package dev.johnmaluki.boardroom_booking_backend.boardrooms.models;

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
@Table(name="locked_boardrooms")
public class LockedRoom extends BaseEntity {
    @Column(name = "boardroom_id")
    private Long boardroomId;

    @Builder.Default
    @Column(name = "locked")
    private boolean locked = true;

    @Column(name = "given_reason")
    private String givenReason;

}
