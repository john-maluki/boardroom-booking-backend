package dev.johnmaluki.boardroom_booking_backend.boardroom.model;

import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import jakarta.persistence.*;
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boardroom_id")
    private Boardroom boardroom;

    @Builder.Default
    @Column(name = "locked")
    private boolean locked = true;

    @Column(name = "given_reason")
    private String givenReason;

}
