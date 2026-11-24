# Database tables

Plain DDL, one file per table, numbered in dependency order (later tables have foreign keys
pointing at earlier ones).

## Runs automatically on startup

The app runs every file here, in filename order, against the database on every startup — see
`DatabaseConfig.connectionFactoryInitializer()`. Every `CREATE TABLE` uses `IF NOT EXISTS`, so
this is safe against a database that already has some or all of the tables: it only creates
what is missing.

To turn this off (e.g. against a shared database the app shouldn't alter, or a prod rollout with
more than one instance starting at the same time — Postgres's `IF NOT EXISTS` guards the common
case but is not a hard lock against truly concurrent creates), set:

```properties
app.datasource.schema-init.enabled=false
```

## Running them by hand

Still works the same way, if you'd rather not rely on the startup step — e.g. to inspect the SQL
before it runs, or to apply it against a database the app itself won't be started against yet.

```bash
for f in src/main/resources/tables/*.sql; do
  psql "postgresql://<user>:<password>@<host>:<port>/<database>" -v ON_ERROR_STOP=1 -f "$f"
done
```

Or concatenate and pipe them in one shot:

```bash
cat src/main/resources/tables/0*.sql | psql "postgresql://<user>:<password>@<host>:<port>/<database>" -v ON_ERROR_STOP=1
```

## Keeping this in sync

Nothing checks these `.sql` files against the Java entities automatically. `EntityColumnMappingTest`
checks the *entity* side only. If you add a column to an entity, add it here too, in the table
whose name matches the entity's `@Table`.

## Table order / dependencies

| File                        | Table               | Depends on            |
|------------------------------|---------------------|------------------------|
| `01_users.sql`               | `users`             | —                      |
| `02_administrators.sql`      | `administrators`    | —                      |
| `03_roles.sql`                | `roles`             | `users`               |
| `04_boardrooms.sql`           | `boardrooms`        | `users`               |
| `05_boardroom_contact.sql`    | `boardroom_contact` | `boardrooms`          |
| `06_equipments.sql`           | `equipments`        | `boardrooms`          |
| `07_locked_boardrooms.sql`    | `locked_boardrooms` | `boardrooms`          |
| `08_reservations.sql`         | `reservations`      | `boardrooms`, `users` |
| `09_notifications.sql`        | `notifications`     | `reservations`        |
