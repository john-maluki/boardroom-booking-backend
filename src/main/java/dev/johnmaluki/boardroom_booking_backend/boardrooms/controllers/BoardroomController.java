package dev.johnmaluki.boardroom_booking_backend.boardrooms.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BoardroomController {
    @GetMapping("/")
    public String index() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return "Welcome to the boardroom page!" + authentication.getAuthorities().toString();
    }
}
