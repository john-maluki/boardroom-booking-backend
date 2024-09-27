package dev.johnmaluki.boardroom_booking_backend.reservation.service.impl;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomServiceUtil;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceOwnershipException;
import dev.johnmaluki.boardroom_booking_backend.core.service.CurrentUserService;
import dev.johnmaluki.boardroom_booking_backend.core.util.DataFilterUtil;
import dev.johnmaluki.boardroom_booking_backend.messaging.email.EmailService;
import dev.johnmaluki.boardroom_booking_backend.reservation.dto.*;
import dev.johnmaluki.boardroom_booking_backend.reservation.mapper.ReservationMapper;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
import dev.johnmaluki.boardroom_booking_backend.reservation.repository.ReservationRepository;
import dev.johnmaluki.boardroom_booking_backend.reservation.service.ReservationService;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.service.UserServiceUtil;
import dev.johnmaluki.boardroom_booking_backend.util.ApprovalStatus;
import dev.johnmaluki.boardroom_booking_backend.util.DateTimeUtil;
import dev.johnmaluki.boardroom_booking_backend.util.EmailUtil;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
    private final DateTimeUtil dateTimeUtil;

    @Override
    public List<ReservationResponseDto> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAllByOrderByStartLocalDateTimeDesc();
        reservations = this.filterUseReservations(reservations);
        return reservationMapper.toReservationResponseDtoList(
                new DataFilterUtil<Reservation>().removeArchivedAndDeletedRecords(reservations)
        );
    }

    @Override
    public List<ReservationResponseDto> getUpcomingReservations() {
        LocalDateTime currentDateTime = dateTimeUtil.getCurrentLocalDateTimeUtc();
        return reservationMapper.toReservationResponseDtoList(
                new DataFilterUtil<Reservation>().removeArchivedAndDeletedRecords(
                        this.filterUseReservations(reservationRepository.findByStartLocalDateTimeAfter(currentDateTime))
                )
        );
    }

    @Override
    public List<ReservationResponseDto> getAllLiveMeetings() {
        LocalDateTime currentDateTime = dateTimeUtil.getCurrentLocalDateTimeUtc();
        return reservationMapper.toReservationResponseDtoList(
                new DataFilterUtil<Reservation>().removeArchivedAndDeletedRecords(
                        this.filterUseReservations(reservationRepository.findLiveMeetings(currentDateTime))
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
        ApprovalStatus approvalStatus = approveReservationDto.approvalStatus();
        reservation.setApprovalStatus(approvalStatus);
        Reservation savedReservation = reservationRepository.save(reservation);
        if(savedReservation.getApprovalStatus() == ApprovalStatus.APPROVED) {
            this.sendEmailToAdminsForMeetLinkCreation(reservation);
        }
        return reservationMapper.toReservationResponseDto(savedReservation);
    }

    @Override
    public ReservationResponseDto updateReservationWithMeetingLink(long reservationId, ReservationMeetingLinkDto reservationMeetingLinkDto) {
        Reservation reservation = this.findReservationByIdFromDb(reservationId);
        boolean wasNUll = reservation.getMeetingLink() == null;
        reservation.setMeetingLink(reservationMeetingLinkDto.meetingLink());
        Reservation savedReservation = reservationRepository.save(reservation);
        if (wasNUll) {
            this.sendEmailToAttendeesPlusMeetingCreator(savedReservation);
        } else {
            String subject = EmailUtil.SUBJECT_RESERVATION_MEETING_LINK_UPDATE;
            this.updateAttendeesPLusAdminsOfMeetingDetailChange(savedReservation, subject);
        }
        return reservationMapper.toReservationResponseDto(savedReservation);
    }

    @Override
    public ReservationResponseDto changeReservationVenue(long reservationId, ChangeVenueDto changeVenueDto) {
        Reservation reservation = this.findReservationByIdFromDb(reservationId);
        Boardroom boardroom = boardroomServiceUtil.findBoardroomById(changeVenueDto.boardroomId());
        reservation.setBoardroom(boardroom);
        reservation.setApprovalStatus(ApprovalStatus.PENDING);
        Reservation savedReservation = reservationRepository.save(reservation);
        if (Objects.equals(savedReservation.getBoardroom().getId(), boardroom.getId())) {
            if (savedReservation.getApprovalStatus() == ApprovalStatus.PENDING) {
                this.sendMailForApproval(boardroom, savedReservation);
            } else if (savedReservation.getApprovalStatus() == ApprovalStatus.APPROVED) {
                String subject = EmailUtil.SUBJECT_RESERVATION_VENUE_CHANGE;
                this.updateAttendeesPLusAdminsOfMeetingDetailChange(savedReservation, subject);
            }
        }
        return reservationMapper.toReservationResponseDto(savedReservation);
    }

    @Override
    public ReservationResponseDto rescheduleReservation(long reservationId, ReservationEventDateDto reservationEventDateDto) {
        LocalDateTime startLocalDateTime = dateTimeUtil.obtainLocalDateTimeFromISOString(reservationEventDateDto.startDateTime());
        LocalDateTime endLocalDateTime = dateTimeUtil.obtainLocalDateTimeFromISOString(reservationEventDateDto.endDateTime());
        Reservation reservation = this.findReservationByIdFromDb(reservationId);
        reservation.setStartLocalDateTime(startLocalDateTime);
        reservation.setEndLocalDateTime(endLocalDateTime);
        Reservation savedReservation = reservationRepository.save(reservation);
        String subject = EmailUtil.SUBJECT_RESERVATION_RESCHEDULED;
        this.updateAttendeesPLusAdminsOfMeetingDetailChange(savedReservation, subject);
        return reservationMapper.toReservationResponseDto(savedReservation);
    }

    @Override
    public void removeReservation(long reservationId) {
        Reservation reservation = this.findReservationByIdFromDb(reservationId);
        boolean isCreationDateLessThan10Minutes = this.checkCreationDateLessThan10Minutes(reservation.getCreatedAt());
        if(isCreationDateLessThan10Minutes) {
            reservationRepository.delete(reservation);
        } else {
            reservation.setDeleted(true);
            reservationRepository.save(reservation);
        }
    }

    private boolean checkCreationDateLessThan10Minutes(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        long minutesDifference = ChronoUnit.MINUTES.between(createdAt, now);
        return minutesDifference <= 10;
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
        boolean isAdminOrBoardroomSupervisor = currentUserService.getUserRole() == RoleType.ADMIN ||
                currentUserService.getUserId() == reservation.getBoardroom().getAdministrator().getId();
        if (isAdminOrBoardroomSupervisor) {
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

    private Set<String> getApplicationAdministratorEmails() {
        return userServiceUtil.getAllSystemAdministrators()
                .stream().map(AppUser::getEmail).collect(Collectors.toSet());
    }

    private void sendEmailToAdminsForMeetLinkCreation(Reservation reservation) {
        Set<String> sendTo = this.getApplicationAdministratorEmails();
        String subject = EmailUtil.SUBJECT_RESERVATION_ADMIN_APPROVAL;
        Map<String, Object> templateModel = this.prepareMailTemplate(reservation);
        emailService.notifyAdministratorsOfReservationApproval(sendTo, subject, templateModel);
    }

    private void sendEmailToAttendeesPlusMeetingCreator(Reservation reservation) {
        Set<String> attendees = this.getAttendeesFromCSVPlusCreator(reservation);
        String subject = EmailUtil.SUBJECT_RESERVATION_MEETING_LINK;
        Map<String, Object> templateModel = this.prepareMailTemplate(reservation);
        emailService.sendEmailToAttendeesPlusCreator(attendees, subject, templateModel);
    }

    private void updateAttendeesPLusAdminsOfMeetingDetailChange(Reservation reservation, String subject) {
        Set<String> attendees = this.getAttendeesFromCSVPlusCreator(reservation);
        Set<String> admins = this.getApplicationAdministratorEmails();
        Map<String, Object> templateModel = this.prepareMailTemplate(reservation);
        templateModel.put("detailChange", true);
        emailService.sendNotificationEmailOfReservationUpdate(attendees, subject, templateModel);
        emailService.sendNotificationEmailOfReservationUpdate(admins, subject, templateModel);
    }

    private Map<String, Object> prepareMailTemplate(Reservation reservation) {
        LocalDateTime startDate = dateTimeUtil.obtainLocalDateTimeBasedOnUserZone(reservation.getStartLocalDateTime(), currentUserService.getAppUserTimezone());
        LocalDateTime endDate = dateTimeUtil.obtainLocalDateTimeBasedOnUserZone(reservation.getEndLocalDateTime(), currentUserService.getAppUserTimezone());
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("boardroomName", reservation.getBoardroom().getName());
        templateModel.put("bookedBy", reservation.getUser().getEmail());
        templateModel.put("meetingType", reservation.getMeetingType());
        templateModel.put("startDate", startDate.toLocalDate());
        templateModel.put("endDate", endDate.toLocalDate());
        templateModel.put("startTime", startDate.toLocalTime());
        templateModel.put("endTime", endDate.toLocalTime());
        templateModel.put("detailChange", false);
        templateModel.put("meetingLink", reservation.getMeetingLink());
        templateModel.put("attendees", this.getAttendeesFromCSVPlusCreator(reservation));
        templateModel.put("meetingTitle", reservation.getMeetingTitle());
        templateModel.put("meetingDescription", reservation.getMeetingDescription());
        return templateModel;
    }


    private Set<String> getAttendeesFromCSVPlusCreator(Reservation reservation) {
        List<String> result = new ArrayList<>();
        String[] items = reservation.getAttendees().split(",");
        for (String item : items) {
            result.add(item.trim());
        }
        String creator = reservation.getUser().getEmail();
        result.add(creator);
        return new HashSet<>(result);
    }

}
