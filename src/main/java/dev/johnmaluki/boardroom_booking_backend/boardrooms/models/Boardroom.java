package dev.johnmaluki.boardroom_booking_backend.boardrooms.models;

import com.unboundid.util.NotNull;
import dev.johnmaluki.boardroom_booking_backend.core.models.BaseEntity;
import dev.johnmaluki.boardroom_booking_backend.equipments.models.Equipment;
import dev.johnmaluki.boardroom_booking_backend.reservations.models.Reservation;
import dev.johnmaluki.boardroom_booking_backend.users.models.AppUser;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name="boardrooms")
public class Boardroom extends BaseEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Lob
    @Column(name = "picture")
    private byte[] picture;

    @Column(name = "description", length = 300, nullable = false)
    private String description;

    @Column(name = "centre")
    private String centre;

    @Column(name = "department")
    private String department;

    @Builder.Default
    @Column(name = "internet_enabled", nullable = false)
    private boolean internetEnabled = true;

    @Column(name = "meeting_type_supported")
    private String meetingTypeSupported; // contains comma separated values

    @Builder.Default
    @Column(name = "locked", nullable = false)
    private boolean locked = false;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrator_id")
    private AppUser administrator;

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "boardroom")
    private List<BoardroomContact> boardroomContacts = new ArrayList<>();

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "boardroom")
    private List<LockedRoom> lockedRooms = new ArrayList<>();

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "boardroom")
    private List<Equipment> equipments = new ArrayList<>();

    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "boardroom")
    private List<Reservation> reservations = new ArrayList<>();


    public void addBoardroomContact(@NotNull BoardroomContact boardroomContact){
            this.boardroomContacts.add(boardroomContact);
            boardroomContact.setBoardroom(this);
    }

    public void removeBoardroomContact(@NotNull BoardroomContact boardroomContact){
        boardroomContact.setBoardroom(null);
        this.boardroomContacts.remove(boardroomContact);
    }

    public void removeBoardroomContacts() {
        this.boardroomContacts.removeIf(
                boardroomContact -> {
                    boardroomContact.setBoardroom(null);
                    return true;
                });
    }

    public void addLockedBoardroom(@NotNull LockedRoom lockedRoom){
        this.lockedRooms.add(lockedRoom);
        lockedRoom.setBoardroom(this);
    }

    public void removeLockedBoardroom(@NotNull LockedRoom lockedRoom){
        lockedRoom.setBoardroom(null);
        this.lockedRooms.remove(lockedRoom);
    }

    public void removeLockedBoardrooms() {
        this.lockedRooms.removeIf(
                lockedRoom -> {
                    lockedRoom.setBoardroom(null);
                    return true;
                });
    }

    // Equipment sync methods
    public void addEquipment(@NotNull Equipment equipment){
        this.equipments.add(equipment);
        equipment.setBoardroom(this);
    }

    public void removeEquipment(@NotNull Equipment equipment){
        equipment.setBoardroom(null);
        this.equipments.remove(equipment);
    }

    public void removeEquipments() {
        this.equipments.removeIf(
                equipment -> {
                    equipment.setBoardroom(null);
                    return true;
                });
    }

    // Reservation sync methods
    public void addReservation(@NotNull Reservation reservation){
        this.reservations.add(reservation);
        reservation.setBoardroom(this);
    }

    public void removeReservation(@NotNull Reservation reservation){
        reservation.setBoardroom(null);
        this.reservations.remove(reservation);
    }

    public void removeReservations() {
        this.reservations.removeIf(
                reservation -> {
                    reservation.setBoardroom(null);
                    return true;
                });
    }

}
