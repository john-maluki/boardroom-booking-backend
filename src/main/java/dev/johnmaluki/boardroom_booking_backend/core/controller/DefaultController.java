package dev.johnmaluki.boardroom_booking_backend.core.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@Hidden
public class DefaultController {
    @GetMapping("/")
    public RedirectView redirectToDefault() {
        return new RedirectView("/swagger-ui/index.html");
    }
}
