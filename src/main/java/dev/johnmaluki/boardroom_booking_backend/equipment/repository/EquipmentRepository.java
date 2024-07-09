package dev.johnmaluki.boardroom_booking_backend.equipment.repository;

import dev.johnmaluki.boardroom_booking_backend.equipment.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    Optional<Equipment> findByIdAndArchivedFalseAndDeletedFalse(long equipmentId);
}
