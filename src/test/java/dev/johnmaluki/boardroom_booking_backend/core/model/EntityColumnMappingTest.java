package dev.johnmaluki.boardroom_booking_backend.core.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.BoardroomContact;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.LockedRoom;
import dev.johnmaluki.boardroom_booking_backend.equipment.model.Equipment;
import dev.johnmaluki.boardroom_booking_backend.notification.model.ApplicationNotification;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppAdmin;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.model.Role;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;

/**
 * Guards the column names Spring Data derives for each entity against the schema declared
 * under {@code src/main/resources/tables/}.
 *
 * <p>This matters more than it looks. R2DBC writes every mapped property on an insert, so a single
 * property mapped to a column the schema does not have breaks all writes to that table — and
 * nothing before a live database would say so. Lombok makes that easy to trip over: for a field
 * named {@code isUrgentMeeting} it generates {@code isUrgentMeeting()}/{@code setUrgentMeeting()},
 * so the JavaBeans property name ({@code urgentMeeting}) differs from the field name, and a mapping
 * that followed the bean name would look for a {@code urgent_meeting} column that does not exist.
 *
 * <p>The expected lists below are the columns declared in {@code db/migration}. The base-entity
 * columns are shared by every table.
 */
class EntityColumnMappingTest {

  private static final List<String> BASE_COLUMNS =
      List.of(
          "id", "tag", "identifier", "occurred_at", "archived", "deleted", "created_at",
          "updated_at", "created_by", "updated_by");

  static Stream<Arguments> entities() {
    return Stream.of(
        Arguments.of(
            Reservation.class,
            List.of(
                "start_date_time", "end_date_time", "approval_status", "cancellation_message",
                "meeting_title", "meeting_description", "meeting_type", "attendees",
                "ict_support_required", "is_urgent_meeting", "record_meeting", "meeting_link",
                "boardroom_id", "user_id")),
        Arguments.of(
            ApplicationNotification.class,
            List.of("title", "message", "recipient", "is_read", "reservation_id")),
        Arguments.of(
            Boardroom.class,
            List.of(
                "name", "email", "capacity", "picture", "description", "centre", "department",
                "internet_enabled", "meeting_type_supported", "locked", "administrator_id")),
        Arguments.of(
            BoardroomContact.class, List.of("boardroom_id", "communication_method", "contact")),
        Arguments.of(LockedRoom.class, List.of("boardroom_id", "locked", "given_reason")),
        Arguments.of(
            Equipment.class,
            List.of(
                "title", "description", "picture", "video_url", "disposed", "model_number",
                "brand", "boardroom_id")),
        Arguments.of(
            AppUser.class,
            List.of(
                "username", "full_name", "department", "email", "enabled", "activated",
                "time_zone")),
        Arguments.of(Role.class, List.of("authority", "user_id")),
        Arguments.of(AppAdmin.class, List.of("email")));
  }

  @ParameterizedTest(name = "{0} maps exactly the columns its table declares")
  @MethodSource("entities")
  void entityMapsExactlyTheSchemaColumns(Class<?> entityType, List<String> ownColumns) {
    Set<String> expected = new TreeSet<>(BASE_COLUMNS);
    expected.addAll(ownColumns);

    Set<String> actual = mappedColumns(entityType);

    Set<String> unexpected = new TreeSet<>(actual);
    unexpected.removeAll(expected);
    Set<String> missing = new TreeSet<>(expected);
    missing.removeAll(actual);

    assertTrue(
        unexpected.isEmpty(),
        () -> entityType.getSimpleName() + " maps columns not in the schema: " + unexpected);
    assertTrue(
        missing.isEmpty(),
        () -> entityType.getSimpleName() + " does not map schema columns: " + missing);
  }

  /** The derived flag is computed per request and must never reach an insert. */
  @Test
  void boardroomDoesNotPersistItsOngoingMeetingFlag() {
    assertTrue(
        mappedColumns(Boardroom.class).stream()
            .noneMatch(column -> column.contains("ongoing")),
        "hasOngoingMeeting must stay transient");
  }

  private Set<String> mappedColumns(Class<?> entityType) {
    R2dbcMappingContext context = new R2dbcMappingContext();
    context.afterPropertiesSet();
    RelationalPersistentEntity<?> entity = context.getRequiredPersistentEntity(entityType);
    Set<String> columns = new TreeSet<>();
    entity.forEach(property -> columns.add(property.getColumnName().getReference()));
    return columns;
  }
}
