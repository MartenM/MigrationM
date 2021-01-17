package nl.martenm.migrationm.api;

import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * Represents a migration file.
 * The InputStreamProvider can be used to retrieve the InputStream in order to read the file.
 * Additionally the file name is also stored.
 */
public class Migration {

    private final LocalDateTime date;
    private final String fileName;

    private final InputStreamProvider provider;

    /**
     * Standard constructor for a migration.
     * @param date The date on which the migration was created.
     * @param fileName The full file name of the migration.
     * @param provider The provider that provides the InputStream of the migration.
     */
    public Migration(LocalDateTime date, String fileName, InputStreamProvider provider) {
        this.date = date;
        this.fileName = fileName;
        this.provider = provider;
    }

    /**
     * Gets the LocalDateTime that this migration has been created on.
     * @return The LocalDateTime of the migration
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * The full name of the migration, including it's date header.
     * @return The full name of the migration file
     */
    public String getName() {
        return fileName;
    }

    /**
     * The InputStream of the migration. Loaded by the InputStreamProvider.
     * @return The InputStream of the migration
     */
    public InputStream getInputStream() {
        return this.provider.getInputStream();
    }
}
