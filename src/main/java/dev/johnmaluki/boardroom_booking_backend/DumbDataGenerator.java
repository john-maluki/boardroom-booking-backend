package dev.johnmaluki.boardroom_booking_backend;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.BoardroomContact;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.LockedRoom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.repository.BoardroomRepository;
import dev.johnmaluki.boardroom_booking_backend.equipment.model.Equipment;
import dev.johnmaluki.boardroom_booking_backend.equipment.repository.EquipmentRepository;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.reservation.repository.ReservationRepository;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.model.Role;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppUserRepository;
import dev.johnmaluki.boardroom_booking_backend.user.repository.RoleRepository;
import dev.johnmaluki.boardroom_booking_backend.util.*;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class DumbDataGenerator implements ApplicationRunner {
    private static final List<RoleType> ROLE_TYPES = Arrays.asList(
            RoleType.ADMIN,
            RoleType.USER,
            RoleType.ADMIN,
            RoleType.USER,
            RoleType.USER,
            RoleType.USER

    );
    private static final List<MeetingType> MEETING_TYPE = Arrays.asList(
            MeetingType.HYBRID,
            MeetingType.PHYSICAL,
            MeetingType.VIRTUAL
    );
    private static final List<String> USERNAMES = Arrays.asList(
            "ben", "bob", "joe", "slashguy"
    );
    private static final List<Long> BOARDROOMS_IDS = Arrays.asList(
            1L, 2L, 3L
    );

    private static final List<Long> USERS_IDS = Arrays.asList(
            1L, 2L, 3L, 4L
    );


    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BoardroomRepository boardroomRepository;
    private final ReservationRepository reservationRepository;
    private final EquipmentRepository equipmentRepository;



    private final Faker faker;
    private final UserUtil userUtil;
    private final Random random = new Random();


    public void createUsers() {
        var users = new ArrayList<AppUser>();
        for (int i = 0; i <= 3; i++) {
            AppUser user = AppUser.builder().email(faker.internet()
                            .emailAddress())
                    .department(faker.company().industry())
                    .username(USERNAMES.get(i))
                    .timeZone(userUtil.getDefaultUserTimeZone())
                    .build();
            users.add(user);
        }
        userRepository.saveAll(users);
    }

    public void createUserRole() {
        var roles = new ArrayList<Role>();
        for (long i = 1; i <= 4; i++) {
            AppUser user = userRepository.findById(i).orElseThrow();
            int index = random.nextInt(DumbDataGenerator.ROLE_TYPES.size());
            RoleType roleType = DumbDataGenerator.ROLE_TYPES.get(index);
            Role role = Role.builder()
                    .authority(roleType)
                    .user(user)
                    .build();

            roles.add(role);
        }
        roleRepository.saveAll(roles);
    }

    public void createBoardrooms(){
        var boardrooms = new ArrayList<Boardroom>();
        for (long i = 1; i <= 3; i++) {
            AppUser user = userRepository.findById(i).orElseThrow();
            BoardroomContact boardroomContact = BoardroomContact.builder()
                    .communicationMethod(CommunicationLineType.PHONE_EXTENSION)
                    .contact(faker.phoneNumber().toString())
                    .build();

            Boardroom boardroom = Boardroom.builder()
                    .administrator(user)
                    .centre(faker.country().capital())
                    .department(faker.company().industry())
                    .capacity(faker.number().numberBetween(20, 60))
                    .name(faker.name().title())
                    .description(faker.text().text(100))
                    .email(faker.internet().emailAddress())
                    .picture(faker.internet().image().getBytes())
                    .meetingTypeSupported("physical,virtual,hybrid")
                    .build();
            boardroom.addBoardroomContact(boardroomContact);
            boardrooms.add(boardroom);
        }

        // create one locked room
        Boardroom boardroom = boardrooms.get(0);
        LockedRoom lockedRoom = LockedRoom.builder()
                .givenReason("The room is being renovated")
                .build();
        boardroom.setLocked(true);
        boardroom.addLockedBoardroom(lockedRoom);

        boardroomRepository.saveAll(boardrooms);
    }

    public void createReservations(){
        var reservations = new ArrayList<Reservation>();
        for (long i = 1; i <= 50; i++) {
            int boardRoomIndex = random.nextInt(DumbDataGenerator.BOARDROOMS_IDS.size());
            long boardroomId = DumbDataGenerator.BOARDROOMS_IDS.get(boardRoomIndex);
            Boardroom boardroom = boardroomRepository.findById(boardroomId).orElseThrow();
            int userIndex = random.nextInt(DumbDataGenerator.USERS_IDS.size());
            long userId = DumbDataGenerator.USERS_IDS.get(userIndex);
            AppUser user = userRepository.findById(userId).orElseThrow();
            int index = random.nextInt(DumbDataGenerator.MEETING_TYPE.size());
            MeetingType meetingType = DumbDataGenerator.MEETING_TYPE.get(index);
            LocalDate startDate = faker.date().birthday().toLocalDateTime().toLocalDate();
            LocalDate endDate = startDate.plusDays(faker.number().numberBetween(1, 30));
            LocalTime startTime = LocalTime.of(faker.number().numberBetween(0, 23), faker.number().numberBetween(0, 59));
            LocalTime endTime = startTime.plusHours(faker.number().numberBetween(1, 5));
            Reservation reservation = Reservation.builder()
                    .attendees("john@test.com,rukia@test.com")
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .boardroom(boardroom)
                    .meetingType(meetingType)
                    .startDate(startDate)
                    .endDate(endDate)
                    .startTime(startTime)
                    .endTime(endTime)
                    .meetingTitle(faker.text().text(10, 20))
                    .meetingDescription(faker.text().text(30, 100))
                    .user(user)
                    .build();
            reservations.add(reservation);
        }

        for (int i = 0; i <=5; i++) {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now();
            LocalTime startTime = LocalTime.now().minusHours(2);
            LocalTime endTime = startTime.plusHours(faker.number().numberBetween(1, 5));
            Reservation reservation = reservations.get(i);
            reservation.setStartDate(startDate);
            reservation.setStartTime(startTime);
            reservation.setEndDate(endDate);
            reservation.setEndTime(endTime);
        }
        reservationRepository.saveAll(reservations);
    }

    public void createBoardroomEquipment() {
        var equipments = new ArrayList<Equipment>();
        for (long i = 1; i <= 20; i++) {
            int boardRoomIndex = random.nextInt(DumbDataGenerator.BOARDROOMS_IDS.size());
            long boardroomId = DumbDataGenerator.BOARDROOMS_IDS.get(boardRoomIndex);
            Boardroom boardroom = boardroomRepository.findById(boardroomId).orElseThrow();
            Equipment equipment = Equipment.builder()
                    .boardroom(boardroom)
                    .title(faker.text().text(5, 15))
                    .description(faker.text().text(15,30))
                    .brand(faker.company().name())
                    .modelNumber(faker.text().text(5, 10))
                    .picture(faker.internet().image().getBytes())
                    .videoUrl(faker.internet().url())
                    .build();
            equipments.add(equipment);
        }
        equipmentRepository.saveAll(equipments);
    }

    @Override
    public void run(ApplicationArguments args)  {
        createUsers();
        createUserRole();
        createBoardrooms();
        createReservations();
        createBoardroomEquipment();
    }
}
