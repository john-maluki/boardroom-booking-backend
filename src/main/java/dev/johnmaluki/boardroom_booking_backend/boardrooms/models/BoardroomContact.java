package dev.johnmaluki.boardroom_booking_backend.boardrooms.models;

import dev.johnmaluki.boardroom_booking_backend.core.models.BaseEntity;
import dev.johnmaluki.boardroom_booking_backend.utils.CommunicationLineType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="boardroom_contact")
public class BoardroomContact extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boardroom_id")
    private Boardroom boardroom;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "communication_method")
    private CommunicationLineType communicationMethod = CommunicationLineType.PHONE_EXTENSION;

    @Column(name = "contact")
    private String contact;
}
