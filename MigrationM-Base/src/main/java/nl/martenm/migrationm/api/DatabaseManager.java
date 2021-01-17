package nl.martenm.migrationm.api;

import nl.martenm.migrationm.api.exceptions.MigrationException;
import nl.martenm.migrationm.util.ProgramInfo;

import java.time.LocalDateTime;

/**
 * The database manager holds no information about the program. It simply knows how to apply the migrations
 * and retrieve info from the given datasource.
 *
 * Migrations are usually used in databases that use SQL but users are free to create their own implementation
 * of Database Managers. This means you can also feed it instructions about a file system in case you ever decide to move files.
 *
 * As a standard the MigrationM-SQL module as been created.
 */
public interface DatabaseManager {

    /**
     * Gets the LocalDateTime of the last migration that was executed.
     * If there has never been a migration executed the method is expected to return NULL.
     * @param info The program info
     * @return The LocalDateTime of the last migration or NULL
     */
    LocalDateTime lastMigration(ProgramInfo info);

    /**
     * Checks if the database has been setup for keeping track of the migrations
     * @return True if the setup has been performed.
     */
    boolean isSetup();

    /**
     * Sets up the database in order to keep track of migrations.
     */
    void setup();

    /**
     * Executes a migration. In case the migration throws an error it's expected that that migration has been fully rolled back.
     * @param info The program info
     * @param migration The migration to be executed.
     * @throws MigrationException A wrapper for exceptions thrown during the executing of the migration.
     */
    void executeMigration(ProgramInfo info, Migration migration) throws MigrationException;
}
