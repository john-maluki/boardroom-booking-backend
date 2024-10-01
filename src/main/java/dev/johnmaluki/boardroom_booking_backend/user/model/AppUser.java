package dev.johnmaluki.boardroom_booking_backend.user.model;

import com.unboundid.util.NotNull;
import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;
import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import dev.johnmaluki.boardroom_booking_backend.reservation.model.Reservation;
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
@Table(name="users")
public class AppUser extends BaseEntity {
    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "fullName", nullable = false)
    private String fullName;

    @Column(name = "department")
    private String department;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Builder.Default
    @Column(name = "enabled")
    private boolean enabled = true;

    @Builder.Default
    @Column(name = "activated")
    private boolean activated = true;

    @Column(name = "time_zone", nullable = false)
    private String timeZone; // Add this field to store the user's time zone

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Role role;

    @ToString.Exclude
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "administrator")
    private List<Boardroom> boardrooms = new ArrayList<>();

    @ToString.Exclude
    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
    private List<Reservation> reservations = new ArrayList<>();


    public void addReservation(@NotNull Reservation reservation){
        this.reservations.add(reservation);
        reservation.setUser(this);
    }

    public void removeReservation(@NotNull Reservation reservation){
        reservation.setUser(null);
        this.reservations.remove(reservation);
    }

    public void removeReservations() {
        this.reservations.removeIf(
                reservation -> {
                    reservation.setUser(null);
                    return true;
                });
    }

    public void addBoardroom(@NotNull Boardroom boardroom){
        this.boardrooms.add(boardroom);
        boardroom.setAdministrator(this);
    }

    public void removeBoardroom(@NotNull Boardroom boardroom){
        boardroom.setAdministrator(null);
        this.boardrooms.remove(boardroom);
    }

    public void removeBoardrooms() {
        this.boardrooms.removeIf(
                boardroom -> {
                    boardroom.setAdministrator(null);
                    return true;
                });
    }

}
