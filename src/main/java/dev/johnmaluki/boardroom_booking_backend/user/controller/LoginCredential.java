package dev.johnmaluki.boardroom_booking_backend.user.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginCredential {
    private String username;
    private String password;
}
