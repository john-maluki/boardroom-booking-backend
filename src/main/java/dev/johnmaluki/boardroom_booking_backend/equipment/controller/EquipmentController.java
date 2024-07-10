package dev.johnmaluki.boardroom_booking_backend.equipment.controller;

import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Equipment")
public class EquipmentController {
    private final EquipmentService equipmentService;

    @GetMapping("/equipments")
    public ResponseEntity<List<EquipmentResponseDto>> getAllEquipments() {
        return ResponseEntity.ok(equipmentService.getAllEquipments());
    }

    @PostMapping("/equipments")
    public ResponseEntity<EquipmentResponseDto> createEquipment(
            @RequestBody @Valid EquipmentDto equipmentDto
            ) {
        return ResponseEntity.ok(equipmentService.createEquipment(equipmentDto));
    }

    @DeleteMapping("/equipments/{equipmentId}")
    @Operation(summary = "Delete the equipment")
    public ResponseEntity<Void> deleteEquipment(
            @PathVariable("equipmentId") long equipmentId
    ) {
        equipmentService.removeEquipment(equipmentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/equipments/{equipmentId}")
    @Operation(summary = "Update the equipment")
    public ResponseEntity<EquipmentResponseDto> updateEquipment(
            @PathVariable("equipmentId") long equipmentId,
            @RequestBody @Valid EquipmentDto equipmentDto
    ) {
        return ResponseEntity.ok(equipmentService.updateEquipment(equipmentId, equipmentDto));
    }
}
