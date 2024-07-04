package dev.johnmaluki.boardroom_booking_backend.boardroom.controller;

import dev.johnmaluki.boardroom_booking_backend.boardroom.dto.BoardroomResponseDto;
import dev.johnmaluki.boardroom_booking_backend.boardroom.service.BoardroomService;
import dev.johnmaluki.boardroom_booking_backend.config.security.UserPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class BoardroomController {
    private final BoardroomService boardroomService;

    @GetMapping("/boardrooms")
    public ResponseEntity<List<BoardroomResponseDto>> getAllBoardrooms() {
        return ResponseEntity.ok(boardroomService.getAllBoardrooms());
    }

    @GetMapping("/boardrooms/{id}")
    public ResponseEntity<BoardroomResponseDto> getBoardroomById(@PathVariable("id") long id) {
        return ResponseEntity.ok(boardroomService.getBoardroomById(id));
    }
}
