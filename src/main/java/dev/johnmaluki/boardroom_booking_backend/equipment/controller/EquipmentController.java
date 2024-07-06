package dev.johnmaluki.boardroom_booking_backend.equipment.controller;

import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.service.EquipmentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class EquipmentController {
    private final EquipmentService equipmentService;

    @GetMapping("/equipments")
    public ResponseEntity<List<EquipmentResponseDto>> getAllEquipments() {
        return ResponseEntity.ok(equipmentService.getAllEquipments());
    }

    @GetMapping("/equipments/{boardroomId}")
    public ResponseEntity<List<EquipmentResponseDto>> getBoardroomEquipments(
            @PathVariable("boardroomId") long boardroomId
    ) {
        return ResponseEntity.ok(equipmentService.getBoardroomEquipments(boardroomId));
    }
}
