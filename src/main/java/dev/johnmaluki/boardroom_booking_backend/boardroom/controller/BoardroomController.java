package dev.johnmaluki.boardroom_booking_backend.boardroom.controller;

import dev.johnmaluki.boardroom_booking_backend.user.service.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BoardroomController {
    @GetMapping("/")
    public String index() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return "Welcome to the boardroom page!" + principal.getUser().getUsername();
    }
}
