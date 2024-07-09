package dev.johnmaluki.boardroom_booking_backend.boardroom.service;

import dev.johnmaluki.boardroom_booking_backend.boardroom.model.Boardroom;

public interface BoardroomServiceUtil {
    public Boardroom findBoardroomById(long boardroomId);
}
