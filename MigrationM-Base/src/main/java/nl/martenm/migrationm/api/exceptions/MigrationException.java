package nl.martenm.migrationm.api.exceptions;

/**
 * A wrapper for semi-expected errors during the execution of a migration.
 */
public class MigrationException extends Exception {

    public MigrationException(String message, Throwable cause) {
        super(message, cause);
    }

}
