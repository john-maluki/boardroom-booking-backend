package dev.johnmaluki.boardroom_booking_backend.core.cache;

/**
 * Every Redis key this application writes.
 *
 * <p>Keys are namespaced by aggregate so that a write can drop a whole family with one pattern
 * delete — changing any boardroom invalidates {@code boardroom:*} rather than requiring the caller
 * to remember each derived entry.
 */
public final class CacheKeys {
  public static final String BOARDROOM_NAMESPACE = "boardroom:*";
  public static final String BOARDROOM_ALL = "boardroom:all";
  public static final String BOARDROOM_LOCKED = "boardroom:locked";
  public static final String BOARDROOM_EQUIPMENT_PREFIX = "boardroom:equipment:";
  public static final String BOARDROOM_CONTACTS_PREFIX = "boardroom:contacts:";

  public static final String EQUIPMENT_NAMESPACE = "equipment:*";
  public static final String EQUIPMENT_ALL = "equipment:all";

  public static final String USER_NAMESPACE = "user:*";
  public static final String USER_ALL = "user:all";
  public static final String USER_BY_ID_PREFIX = "user:id:";
  public static final String USER_TIMEZONES = "user:timezones";
  public static final String USER_SYSTEM_ADMINS = "user:system-admins";

  private CacheKeys() {}

  public static String boardroomById(long boardroomId) {
    return "boardroom:id:" + boardroomId;
  }

  public static String boardroomEquipment(long boardroomId) {
    return BOARDROOM_EQUIPMENT_PREFIX + boardroomId;
  }

  public static String boardroomContacts(long boardroomId) {
    return BOARDROOM_CONTACTS_PREFIX + boardroomId;
  }

  public static String userById(long userId) {
    return USER_BY_ID_PREFIX + userId;
  }
}
