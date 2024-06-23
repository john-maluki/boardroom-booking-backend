package dev.johnmaluki.boardroom_booking_backend.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * An abstract base class model that adds a archived field to any inheriting model, allowing
 *     for archiving.
 * <br/><br/>
 *     Instead of records being removed from the database, the active field is used
 *     to mark entities as active. This approach preserves the active entities for audit or other
 *     purposes while effectively hiding them from normal application operations.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@MappedSuperclass
public abstract class Archival extends Identifiable{
    @Builder.Default
    @Column(name = "archived", nullable = false)
    private Boolean archived = false;
}
