package dev.johnmaluki.boardroom_booking_backend;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.BoardroomContact;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.LockedRoom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.repository.BoardroomContactRepository;
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
import net.datafaker.Faker;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class DumbDataGenerator implements ApplicationRunner {
    private static final List<RoleType> ROLE_TYPES = Arrays.asList(
            RoleType.ADMIN,
            RoleType.USER,
            RoleType.ADMIN,
            RoleType.USER,
            RoleType.USER,
            RoleType.USER

    );
    private static final List<MeetingType> MEETTING_TYPE = Arrays.asList(
            MeetingType.HYBRID,
            MeetingType.PHYSICAL,
            MeetingType.VIRTUAL
    );

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BoardroomRepository boardroomRepository;
    private final ReservationRepository reservationRepository;
    private final BoardroomContactRepository boardroomContactRepository;
    private final EquipmentRepository equipmentRepository;



    private final Faker faker;
    private final UserUtil userUtil;
    private final Random random = new Random();

    public DumbDataGenerator(AppUserRepository userRepository, RoleRepository roleRepository, BoardroomRepository boardroomRepository, ReservationRepository reservationRepository, BoardroomContactRepository boardroomContactRepository, EquipmentRepository equipmentRepository, Faker faker, UserUtil userUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.boardroomRepository = boardroomRepository;
        this.reservationRepository = reservationRepository;
        this.boardroomContactRepository = boardroomContactRepository;
        this.equipmentRepository = equipmentRepository;
        this.faker = faker;
        this.userUtil = userUtil;
    }

    public void createUsers() {
        var users = new ArrayList<AppUser>();
        for (long i = 1; i <= 10; i++) {
            AppUser user = AppUser.builder().email(faker.internet()
                            .emailAddress())
                    .department(faker.company().industry())
                    .username(faker.internet().username())
                    .timeZone(userUtil.getDefaultUserTimeZone())
                    .build();
            users.add(user);
        }
        userRepository.saveAll(users);
    }

    public void createUserRole() {
        var roles = new ArrayList<Role>();
        for (long i = 1; i <= 10; i++) {
            AppUser user = userRepository.findById(i).get();
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
        for (long i = 1; i <= 10; i++) {
            AppUser user = userRepository.findById(i).get();
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
                    .meetingTypeSupported(faker.text().text(10))
                    .build();
            boardroom.addBoardroomContact(boardroomContact);
            boardrooms.add(boardroom);
        }

        // create one locked room
        Boardroom boardroom = boardrooms.get(0);
        LockedRoom lockedRoom = LockedRoom.builder()
                .givenReason("The room is being renovated")
                .build();
        boardroom.addLockedBoardroom(lockedRoom);

        boardroomRepository.saveAll(boardrooms);
    }

    public void createReservations(){
        var reservations = new ArrayList<Reservation>();
        for (long i = 1; i <= 10; i++) {
            Boardroom boardroom = boardroomRepository.findById(i).get();
            AppUser user = userRepository.findById(i).get();
            int index = random.nextInt(DumbDataGenerator.MEETTING_TYPE.size());
            MeetingType meetingType = DumbDataGenerator.MEETTING_TYPE.get(index);
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
        reservationRepository.saveAll(reservations);
    }

    public void createBoardroomEquipment() {
        var equipments = new ArrayList<Equipment>();
        for (long i = 1; i <= 10; i++) {
            Boardroom boardroom = boardroomRepository.findById(i).get();
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
    public void run(ApplicationArguments args) throws Exception {
        createUsers();
        createUserRole();
        createBoardrooms();
        createReservations();
        createBoardroomEquipment();
    }
}
