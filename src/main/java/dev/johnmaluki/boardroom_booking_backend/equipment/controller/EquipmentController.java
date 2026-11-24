package dev.johnmaluki.boardroom_booking_backend.equipment.controller;

import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.dto.EquipmentResponseDto;
import dev.johnmaluki.boardroom_booking_backend.equipment.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Tag(name = "Equipment")
public class EquipmentController {
  private final EquipmentService equipmentService;

  @GetMapping("/equipments")
  public Flux<EquipmentResponseDto> getAllEquipments() {
    return equipmentService.getAllEquipments();
  }

  @PostMapping("/equipments")
  public Mono<EquipmentResponseDto> createEquipment(@RequestBody @Valid EquipmentDto equipmentDto) {
    return equipmentService.createEquipment(equipmentDto);
  }

  @DeleteMapping("/equipments/{equipmentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete the equipment")
  public Mono<Void> deleteEquipment(@PathVariable("equipmentId") long equipmentId) {
    return equipmentService.removeEquipment(equipmentId);
  }

  @PatchMapping("/equipments/{equipmentId}")
  @Operation(summary = "Update the equipment")
  public Mono<EquipmentResponseDto> updateEquipment(
      @PathVariable("equipmentId") long equipmentId, @RequestBody @Valid EquipmentDto equipmentDto) {
    return equipmentService.updateEquipment(equipmentId, equipmentDto);
  }
}
