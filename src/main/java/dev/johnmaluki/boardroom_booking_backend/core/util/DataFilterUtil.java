package dev.johnmaluki.boardroom_booking_backend.core.util;

import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;

import java.util.List;

public final class DataFilterUtil<T extends BaseEntity> {
    public List<T> removeArchivedAndDeletedRecords(List<T> items) {
        return items.stream().filter(
                item -> !item.getArchived() && !item.getDeleted()
        ).toList();
    }

    public List<T> removeDeletedRecords(List<T> items) {
        return items.stream().filter(
                item -> !item.getArchived() && !item.getDeleted()
        ).toList();
    }

}
