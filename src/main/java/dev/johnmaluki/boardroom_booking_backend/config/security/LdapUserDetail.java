package dev.johnmaluki.boardroom_booking_backend.config.security;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LdapUserDetail {
    private String username;
    private String name;
    private String email;
}
