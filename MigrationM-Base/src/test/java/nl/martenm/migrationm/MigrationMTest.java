package nl.martenm.migrationm;


import nl.martenm.migrationm.api.Migration;
import nl.martenm.migrationm.util.ProgramInfo;
import nl.martenm.migrationm.impl.TestDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;

public class MigrationMTest {

    Logger logger = Logger.getAnonymousLogger();

    @Test
    public void testAddMigration() {
        ProgramInfo info = new ProgramInfo("test-program", "1.0.0", logger);

        TestDatabase testDatabase = new TestDatabase();
        testDatabase.setLatestMigration(info, LocalDateTime.of(2020, 1, 1, 1, 1));

        MigrationM migrationM = new MigrationM(info, testDatabase);

        File file = new File("2020-2-2-22.22.sql");
        migrationM.addMigration(file, null);

        assert migrationM.migrations.get(0).getName().equals(file.getName());
        assert migrationM.migrations.get(0).getDate().equals(LocalDateTime.of(2020, 2, 2, 22, 22));
    }

    @Test()
    public void testAddMalformedName() {
        ProgramInfo info = new ProgramInfo("test-program", "1.0.0", logger);

        TestDatabase testDatabase = new TestDatabase();
        testDatabase.setLatestMigration(info, LocalDateTime.of(2020, 1, 1, 1, 1));

        MigrationM migrationM = new MigrationM(info, testDatabase);

        File file1 = new File("2020-232-2-22.22.sdfql");
        File file2 = new File("2020-2-2-22.22-testt.sqlafds");

        Assertions.assertThrows(DateTimeParseException.class, () -> {
            migrationM.addMigration(file1, null);
        });

        migrationM.addMigration(file2, null);
    }

    @Test
    public void testGetRequiredAfter() {
        ProgramInfo info = new ProgramInfo("test-program", "1.0.0", logger);

        TestDatabase testDatabase = new TestDatabase();
        testDatabase.setLatestMigration(info, LocalDateTime.of(2020, 2, 1, 1, 1));

        MigrationM migrationM = new MigrationM(info, testDatabase);
        File migration1 = new File("2021-2-1-10.50-awesome_migration.sql");
        File migration2 = new File("2020-1-10-14.00-another_migration.sql");
        File migration3 = new File("2020-2-5-10.01.sql");

        migrationM.addMigration(migration1, null);
        migrationM.addMigration(migration2, null);
        migrationM.addMigration(migration3, null);

        assert migrationM.getRequiredMigrations().size() == 2;
    }

    @Test
    public void testGetRequiredOrder() {
        ProgramInfo info = new ProgramInfo("test-program", "1.0.0", logger);

        TestDatabase testDatabase = new TestDatabase();
        testDatabase.setLatestMigration(info, LocalDateTime.of(2020, 1, 1, 1, 1));

        MigrationM migrationM = new MigrationM(info, testDatabase);


        File migration1 = new File("2021-2-1-10.50_create_database_schema.sql");
        File migration2 = new File("2020-1-10-14.00.sql");
        File migration3 = new File("2020-2-5-10.01.sql");
        File migration4 = new File("2020-3-1-20.00.sql");


        migrationM.addMigration(migration1,null);
        migrationM.addMigration(migration2, null);
        migrationM.addMigration(migration3, null);
        migrationM.addMigration(migration4, null);

        List<Migration> migrationList = migrationM.getRequiredMigrations();

        LocalDateTime previous = migrationList.get(0).getDate();
        for (int i = 1; i < migrationList.size(); i++) {
            LocalDateTime date = migrationList.get(i).getDate();
            assert date.isAfter(previous);
            previous = date;
        }
    }

    @Test
    public void testMigrateSetup() {
        ProgramInfo info = new ProgramInfo("test-program", "1.0.0", logger);

        TestDatabase testDatabase = new TestDatabase();
        testDatabase.setLatestMigration(info, LocalDateTime.of(2020, 1, 1, 1, 1));

        MigrationM migrationM = new MigrationM(info, testDatabase);

        migrationM.migrate();
        assert testDatabase.isSetup();
    }

    @Test
    public void testMigrateExecution() {
        ProgramInfo info = new ProgramInfo("test-program", "1.0.0", logger);

        TestDatabase testDatabase = new TestDatabase();
        testDatabase.setLatestMigration(info, LocalDateTime.of(2020, 1, 1, 1, 1));

        MigrationM migrationM = new MigrationM(info, testDatabase);

        File migration1 = new File("2021-2-1-10.50.sql");
        File migration2 = new File("2020-1-10-14.00.sql");
        File migration3 = new File("2020-2-5-10.01.sql");
        File migration4 = new File("2020-3-1-20.00.sql");


        migrationM.addMigration(migration1, null);
        migrationM.addMigration(migration2, null);
        migrationM.addMigration(migration3, null);
        migrationM.addMigration(migration4, null);

        migrationM.migrate();
        assert testDatabase.isSetup();
        assert testDatabase.lastMigration(info).equals(LocalDateTime.of(2021, 2, 1, 10, 50));
    }
}
