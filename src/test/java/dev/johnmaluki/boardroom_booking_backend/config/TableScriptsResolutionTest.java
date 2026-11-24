package dev.johnmaluki.boardroom_booking_backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Pins down the exact mechanism {@code DatabaseConfig.tableScripts()} uses to find and order the
 * DDL under {@code src/main/resources/tables/}: {@code classpath:tables/*.sql} resolved via
 * {@link PathMatchingResourcePatternResolver}, then sorted by filename.
 *
 * <p>This matters more than it looks. The resource pattern resolver makes no promise about scan
 * order on its own — it can differ between an exploded {@code classes/} directory (as in this
 * test and local {@code bootRun}) and a packaged jar — so without an explicit sort, a table could
 * get created before the table its foreign key points at, and the run would fail. This test would
 * catch both a resolution problem (the wrong set of files found) and an ordering problem (the
 * right files, wrong order) before either shows up against a real database.
 */
class TableScriptsResolutionTest {

  private static final List<String> EXPECTED_ORDER =
      List.of(
          "01_users.sql",
          "02_administrators.sql",
          "03_roles.sql",
          "04_boardrooms.sql",
          "05_boardroom_contact.sql",
          "06_equipments.sql",
          "07_locked_boardrooms.sql",
          "08_reservations.sql",
          "09_notifications.sql");

  @Test
  void resolvesEveryTableScriptInDependencyOrder() throws Exception {
    Resource[] resources =
        new PathMatchingResourcePatternResolver().getResources("classpath:tables/*.sql");
    Arrays.sort(resources, Comparator.comparing(Resource::getFilename));

    List<String> filenames = Arrays.stream(resources).map(Resource::getFilename).toList();

    assertEquals(EXPECTED_ORDER, filenames);
  }
}
