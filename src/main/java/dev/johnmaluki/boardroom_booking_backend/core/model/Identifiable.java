package dev.johnmaluki.boardroom_booking_backend.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * An abstract base class model that provides a universally unique identifier (UUID) for each
 *     instance of any model that inherits from it.
 * <br/>
 * <br/>
 *     It features an identifier field for external references or additional identification needs.
 *     occurred_at tracks the occurrence of a specific event
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@SuperBuilder
@MappedSuperclass
public abstract class Identifiable {
    @Column(updatable = false, unique = true)
    private String identifier;

    @Builder.Default
    @Column(name = "occurred_at")
    private LocalDateTime occurredAt = LocalDateTime.now();

    @PrePersist // Generate UUID before persisting the entity
    public void generateIdentifierUUIDValue(){
        if (this.identifier == null) {
            this.identifier = UUID.randomUUID().toString();
        }
    }
}
