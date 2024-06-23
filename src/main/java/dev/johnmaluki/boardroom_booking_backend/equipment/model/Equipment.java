package dev.johnmaluki.boardroom_booking_backend.equipment.model;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
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
@Table(name="equipments")
public class Equipment extends BaseEntity {
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 300)
    private String description;

    @Lob
    @Column(name = "picture")
    private byte[] picture;

    @Column(name = "video_url")
    private String videoUrl;

    @Builder.Default
    @Column(name = "disposed")
    private boolean disposed = false;

    @Column(name = "model_number")
    private String modelNumber;

    @Column(name = "brand")
    private String brand;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boardroom_id")
    private Boardroom boardroom;

}
