package dev.johnmaluki.boardroom_booking_backend.reservation.service.impl;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomServiceUtil;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceOwnershipException;
import dev.johnmaluki.boardroom_booking_backend.core.service.CurrentUserService;
import dev.johnmaluki.boardroom_booking_backend.core.util.DataFilterUtil;
import dev.johnmaluki.boardroom_booking_backend.messaging.email.EmailService;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ApproveReservationDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.ReservationResponseDto;
import dev.johnmaluki.boardroom_booking_backend.reservation.mapper.ReservationMapper;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.reservation.repository.ReservationRepository;
import dev.johnmaluki.boardroom_booking_backend.reservation.service.ReservationService;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.service.UserServiceUtil;
import dev.johnmaluki.boardroom_booking_backend.util.ApprovalStatus;
import dev.johnmaluki.boardroom_booking_backend.util.EmailUtil;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {
    private final CurrentUserService currentUserService;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private  final BoardroomServiceUtil boardroomServiceUtil;
    private final EmailService emailService;
    private final UserServiceUtil userServiceUtil;

    @Override
    public List<ReservationResponseDto> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        reservations = this.filterUseReservations(reservations);
        return reservationMapper.toReservationResponseDtoList(
                new DataFilterUtil<Reservation>().removeArchivedAndDeletedRecords(reservations)
        );
    }

    @Override
    public List<ReservationResponseDto> getUpcomingReservations() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return reservationMapper.toReservationResponseDtoList(
                new DataFilterUtil<Reservation>().removeArchivedAndDeletedRecords(
                        this.filterUseReservations(reservationRepository.findByStartDateAndStartTimeGreaterThan(today, now))
                )
        );
    }

    @Override
    public List<ReservationResponseDto> getAllLiveMeetings() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        return reservationMapper.toReservationResponseDtoList(
                new DataFilterUtil<Reservation>().removeArchivedAndDeletedRecords(
                        this.filterUseReservations(reservationRepository.findLiveMeetings(today, now))
                )
        );
    }

    @Override
    public List<ReservationResponseDto> getArchivedReservations() {
        return reservationMapper.toReservationResponseDtoList(
                this.filterUseReservations(reservationRepository.findByArchivedTrueAndDeletedFalse())
        );
    }

    @Override
    public ReservationResponseDto getReservationById(long reservationId) {
        Reservation reservation = this.findReservationByIdFromDb(reservationId);
        if (!this.hasOwnership(reservation)) {
            throw new ResourceOwnershipException("Not allowed to access");
        }
        return reservationMapper.toReservationResponseDto(reservation);
    }

    @Override
    public ReservationResponseDto createReservation(ReservationDto reservationDto) {
        Boardroom boardroom = boardroomServiceUtil.findBoardroomById(reservationDto.boardroomId());
        AppUser user = currentUserService.getAppUser();
        Reservation reservation = reservationMapper.toReservation(reservationDto);
        reservation.setBoardroom(boardroom);
        reservation.setUser(user);
        Reservation savedReservation = reservationRepository.save(reservation);
        // After successful saving do sent email to boardroom admin for approval
        if(savedReservation.getId() != null) {
            this.sendMailForApproval(boardroom, savedReservation);
        }
        return reservationMapper.toReservationResponseDto(savedReservation);
    }

    @Override
    public ReservationResponseDto approveReservation(long reservationId, ApproveReservationDto approveReservationDto) {
        Reservation reservation = this.findReservationByIdFromDb(reservationId);
        reservation.setApprovalStatus(ApprovalStatus.APPROVED);
        Reservation savedReservation = reservationRepository.save(reservation);
        if(savedReservation.getApprovalStatus() == ApprovalStatus.APPROVED) {
            this.sendEmailToAdminsForMeetLinkCreation(reservation);
        }
        return reservationMapper.toReservationResponseDto(savedReservation);
    }

    private List<Reservation> filterUseReservations(List<Reservation> reservations) {
        if (currentUserService.getUserRole() != RoleType.ADMIN) {
            return reservations.stream().filter(
                    reservation -> reservation.getUser().getId() == currentUserService.getUserId()
            ).toList();
        } else {
            return reservations;
        }
    }

    private boolean hasOwnership(Reservation reservation) {
        if (currentUserService.getUserRole() == RoleType.ADMIN) {
            return true;
        }
        return currentUserService.getUserId() == reservation.getUser().getId();
    }
    private Reservation findReservationByIdFromDb(long reservationId) {
        return reservationRepository.findByIdAndArchivedFalseAndDeletedFalse(reservationId).orElseThrow(
                () -> new ResourceNotFoundException("Reservation could not be found")
        );
    }

    private void sendMailForApproval(Boardroom boardroom, Reservation reservation) {
        String to = boardroom.getAdministrator().getEmail();
        String subject = EmailUtil.SUBJECT_RESERVATION_CONFIRMATION;
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("boardroomName", boardroom.getName());
        templateModel.put("meetingTitle", reservation.getMeetingTitle());
        templateModel.put("meetingDescription", reservation.getMeetingDescription());
        emailService.sendEmailForReservationApproval(to, subject,templateModel);
    }

    private List<String> getApplicationAdministratorEmails() {
        return userServiceUtil.getAllSystemAdministrators()
                .stream().map(AppUser::getEmail).toList();
    }

    private void sendEmailToAdminsForMeetLinkCreation(Reservation reservation) {
        List<String> to = this.getApplicationAdministratorEmails();
        String subject = EmailUtil.SUBJECT_RESERVATION_ADMIN_APPROVAL;
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("boardroomName", reservation.getBoardroom().getName());
        templateModel.put("bookedBy", reservation.getUser().getEmail());
        templateModel.put("meetingType", reservation.getMeetingType());
        templateModel.put("startDate", reservation.getStartDate());
        templateModel.put("endDate", reservation.getEndDate());
        templateModel.put("startTime", reservation.getStartTime());
        templateModel.put("endTime", reservation.getEndTime());
        templateModel.put("attendees", this.getAttendeesFromCSV(reservation.getAttendees()));
        templateModel.put("meetingTitle", reservation.getMeetingTitle());
        templateModel.put("meetingDescription", reservation.getMeetingDescription());
        emailService.notifyAdministratorsOfReservationApproval(to, subject, templateModel);
    }

    private List<String> getAttendeesFromCSV(String attendeesCSV) {
        List<String> result = new ArrayList<>();
        String[] items = attendeesCSV.split(",");
        for (String item : items) {
            result.add(item.trim());
        }
        return result;
    }

}
