package dev.johnmaluki.boardroom_booking_backend;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.BoardroomContact;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.LockedRoom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.repository.BoardroomContactRepository;
import dev.johnmaluki.boardroom_booking_backend.boardroom.repository.BoardroomRepository;
import dev.johnmaluki.boardroom_booking_backend.boardroom.repository.LockedRoomRepository;
import dev.johnmaluki.boardroom_booking_backend.equipment.model.Equipment;
import dev.johnmaluki.boardroom_booking_backend.equipment.repository.EquipmentRepository;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.reservation.repository.ReservationRepository;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.model.Role;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppUserRepository;
import dev.johnmaluki.boardroom_booking_backend.user.repository.RoleRepository;
import dev.johnmaluki.boardroom_booking_backend.util.ApprovalStatus;
import dev.johnmaluki.boardroom_booking_backend.util.CommunicationLineType;
import dev.johnmaluki.boardroom_booking_backend.util.MeetingType;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import dev.johnmaluki.boardroom_booking_backend.util.UserUtil;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Fills a fresh dev database with something to look at.
 *
 * <p>Two changes from the JPA version. Associations are gone, so each stage saves its parent first
 * and threads the generated id into the children rather than relying on cascades. And the seed is
 * now skipped when users already exist: Hibernate used to recreate the schema on every start, so a
 * second run was impossible; now that schema creation is a one-off manual step (see {@code
 * src/main/resources/tables/}), the tables persist across restarts, so without this guard a
 * second app start would seed a second, duplicate set of data.
 */
@Profile("dev")
@Component
@RequiredArgsConstructor
@Slf4j
public class DumbDataGeneratorRunner implements ApplicationRunner {
  private static final Duration SEED_TIMEOUT = Duration.ofMinutes(2);

  private static final List<MeetingType> MEETING_TYPES =
      Arrays.asList(MeetingType.HYBRID, MeetingType.PHYSICAL);
  private static final List<ApprovalStatus> APPROVAL_STATUSES =
      Arrays.asList(ApprovalStatus.APPROVED, ApprovalStatus.PENDING, ApprovalStatus.DECLINED);
  private static final List<String> USERNAMES = Arrays.asList("ben", "bob", "joe", "slashguy");
  private static final List<String> IMAGES = Arrays.asList("1.jpg", "2.jpg", "3.jpg", "4.jpg");
  private static final List<String> EQUIPMENT_IMAGES =
      Arrays.asList("6.jpg", "6.jpg", "7.jpg", "7.jpg");
  private static final int BOARDROOM_COUNT = 3;
  private static final int RESERVATION_COUNT = 15;
  private static final int EQUIPMENT_COUNT = 7;
  private static final int LIVE_MEETING_COUNT = 6;

  private final AppUserRepository userRepository;
  private final RoleRepository roleRepository;
  private final BoardroomRepository boardroomRepository;
  private final BoardroomContactRepository boardroomContactRepository;
  private final LockedRoomRepository lockedRoomRepository;
  private final ReservationRepository reservationRepository;
  private final EquipmentRepository equipmentRepository;
  private final Faker faker;
  private final UserUtil userUtil;
  private final Random random = new Random();

  @Override
  public void run(ApplicationArguments args) {
    userRepository
        .count()
        .flatMap(existing -> {
          if (existing > 0) {
            log.info("Dev data already present ({} users); skipping seed", existing);
            return Mono.empty();
          }
          return seed();
        })
        .block(SEED_TIMEOUT);
  }

  private Mono<Void> seed() {
    log.info("Seeding dev data");
    return createUsers()
        .flatMap(users -> createUserRoles(users).thenReturn(users))
        .flatMap(users ->
            createBoardrooms(users)
                .flatMap(boardrooms ->
                    createReservations(users, boardrooms)
                        .then(createBoardroomEquipment(boardrooms))))
        .then();
  }

  private Mono<List<AppUser>> createUsers() {
    List<AppUser> users = new ArrayList<>();
    for (String username : USERNAMES) {
      users.add(
          AppUser.builder()
              .email(faker.internet().emailAddress())
              .department(faker.company().industry())
              .fullName(faker.name().fullName())
              .username(username)
              .timeZone(userUtil.getDefaultUserTimeZone())
              .build());
    }
    AppUser first = users.get(0);
    first.setEmail("jmuimi@kemri.go.ke");
    first.setFullName("John Maluki");
    return userRepository.saveAll(users).collectList();
  }

  /** The first seeded user is made an administrator so the dev app has one to log in as. */
  private Mono<Void> createUserRoles(List<AppUser> users) {
    List<Role> roles = new ArrayList<>();
    for (int i = 0; i < users.size(); i++) {
      roles.add(
          Role.builder()
              .authority(i == 0 ? RoleType.ADMIN : RoleType.USER)
              .userId(users.get(i).getId())
              .build());
    }
    return roleRepository.saveAll(roles).then();
  }

  private Mono<List<Boardroom>> createBoardrooms(List<AppUser> users) {
    List<Boardroom> boardrooms = new ArrayList<>();
    for (int i = 0; i < BOARDROOM_COUNT; i++) {
      boardrooms.add(
          Boardroom.builder()
              .administratorId(users.get(i % users.size()).getId())
              .centre(faker.country().capital())
              .department(faker.company().industry())
              .capacity(faker.number().numberBetween(20, 60))
              .name(faker.name().title() + " " + (i + 1))
              .description(faker.lorem().paragraph(8))
              .email(faker.internet().emailAddress())
              .picture(IMAGES.get(random.nextInt(IMAGES.size())))
              .meetingTypeSupported("Physical,Hybrid")
              // The second room is locked, to exercise the lock screens.
              .locked(i == 1)
              .build());
    }
    return boardroomRepository
        .saveAll(boardrooms)
        .collectList()
        .flatMap(saved -> createContactsAndLocks(saved).thenReturn(saved));
  }

  private Mono<Void> createContactsAndLocks(List<Boardroom> boardrooms) {
    List<BoardroomContact> contacts =
        boardrooms.stream()
            .<BoardroomContact>map(boardroom ->
                BoardroomContact.builder()
                    .boardroomId(boardroom.getId())
                    .communicationMethod(CommunicationLineType.PHONE_EXTENSION)
                    .contact(faker.phoneNumber().extension())
                    .build())
            .toList();

    List<LockedRoom> locks =
        boardrooms.stream()
            .filter(Boardroom::isLocked)
            .<LockedRoom>map(boardroom ->
                LockedRoom.builder()
                    .boardroomId(boardroom.getId())
                    .locked(true)
                    .givenReason("The room is being renovated")
                    .build())
            .toList();

    return boardroomContactRepository
        .saveAll(contacts)
        .then(lockedRoomRepository.saveAll(locks).then());
  }

  private Mono<Void> createReservations(List<AppUser> users, List<Boardroom> boardrooms) {
    List<Reservation> reservations = new ArrayList<>();
    for (int i = 0; i < RESERVATION_COUNT; i++) {
      LocalDateTime startDate =
          faker
              .date()
              .future(1, TimeUnit.DAYS)
              .toInstant()
              .atZone(ZoneId.of("UTC"))
              .toLocalDateTime()
              .withHour(faker.number().numberBetween(6, 19))
              .withMinute(0)
              .withSecond(0)
              .truncatedTo(ChronoUnit.SECONDS);
      LocalDateTime endDate = startDate.plusDays(faker.number().numberBetween(0, 1)).withHour(19);

      // The first few are moved onto the current time so the live-meetings view has content.
      if (i < LIVE_MEETING_COUNT) {
        startDate = LocalDateTime.now(ZoneId.of("UTC"));
        endDate = startDate.plusHours(faker.number().numberBetween(1, 5));
      }

      reservations.add(
          Reservation.builder()
              .attendees("john@test.com,rukia@test.com")
              .approvalStatus(APPROVAL_STATUSES.get(random.nextInt(APPROVAL_STATUSES.size())))
              .boardroomId(boardrooms.get(random.nextInt(boardrooms.size())).getId())
              .userId(users.get(random.nextInt(users.size())).getId())
              .meetingType(MEETING_TYPES.get(random.nextInt(MEETING_TYPES.size())))
              .startLocalDateTime(startDate)
              .endLocalDateTime(endDate)
              .meetingTitle(faker.lorem().paragraph(1))
              .meetingDescription(faker.lorem().paragraph(20))
              .build());
    }
    return reservationRepository.saveAll(reservations).then();
  }

  private Mono<Void> createBoardroomEquipment(List<Boardroom> boardrooms) {
    List<Equipment> equipments = new ArrayList<>();
    for (int i = 0; i < EQUIPMENT_COUNT; i++) {
      equipments.add(
          Equipment.builder()
              .boardroomId(boardrooms.get(random.nextInt(boardrooms.size())).getId())
              .title(faker.device().modelName())
              .description(faker.lorem().paragraph(5))
              .brand(faker.device().manufacturer())
              .modelNumber(faker.device().serial())
              .picture(EQUIPMENT_IMAGES.get(random.nextInt(EQUIPMENT_IMAGES.size())))
              .videoUrl("https://www.youtube.com/watch?v=hketeOJPlTs")
              .build());
    }
    return equipmentRepository.saveAll(equipments).then();
  }
}
