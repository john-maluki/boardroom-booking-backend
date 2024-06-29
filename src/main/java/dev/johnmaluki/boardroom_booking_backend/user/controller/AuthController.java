package dev.johnmaluki.boardroom_booking_backend.user.controller;

import dev.johnmaluki.boardroom_booking_backend.user.dto.AuthenticationResponse;
import dev.johnmaluki.boardroom_booking_backend.user.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody LoginCredential loginCredential
    ) {
        System.out.println("login 1");
        return  ResponseEntity.ok(authenticationService.login(loginCredential.getUsername(), loginCredential.getPassword()));
    }

}
