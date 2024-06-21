package dev.johnmaluki.boardroom_booking_backend.core.models;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@MappedSuperclass
public abstract class BaseEntity extends AuditableAndDeletable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag", unique = true, nullable = false)
    private String tag;

    @PrePersist // Generate UUID before persisting the entity
    public void generateTagUUIDValue(){
        if (this.tag == null) {
            this.tag = UUID.randomUUID().toString();
        }
    }
}
