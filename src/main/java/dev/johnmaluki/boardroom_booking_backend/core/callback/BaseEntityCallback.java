package dev.johnmaluki.boardroom_booking_backend.core.callback;

import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import java.util.UUID;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.core.publisher.Mono;

/**
 * Populates the {@code tag} and {@code identifier} UUIDs before a row is written.
 *
 * <p>This is the R2DBC stand-in for the {@code @PrePersist} hooks the JPA entities used to carry.
 * Because the callback is declared against {@link BaseEntity}, Spring Data resolves it for every
 * entity in the hierarchy, so there is one hook rather than one per table. Both columns are
 * {@code UNIQUE} in the schema and {@code tag} is {@code NOT NULL}, so an insert without them
 * fails; filling them only when absent leaves values loaded from the database untouched.
 */
public class BaseEntityCallback implements BeforeConvertCallback<BaseEntity> {

  @Override
  public Publisher<BaseEntity> onBeforeConvert(BaseEntity entity, SqlIdentifier table) {
    if (entity.getTag() == null) {
      entity.setTag(UUID.randomUUID().toString());
    }
    if (entity.getIdentifier() == null) {
      entity.setIdentifier(UUID.randomUUID().toString());
    }
    return Mono.just(entity);
  }

}
