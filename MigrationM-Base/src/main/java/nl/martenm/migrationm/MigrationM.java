package nl.martenm.migrationm;

import nl.martenm.migrationm.api.DatabaseManager;
import nl.martenm.migrationm.api.InputStreamProvider;
import nl.martenm.migrationm.api.Migration;
import nl.martenm.migrationm.util.ProgramInfo;

import java.io.*;
import java.net.URL;
import java.security.CodeSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The MigrationM class takes care of all the logic that comes with locating the migrations
 * and applying the right ones.
 *
 * It's able to load migrations on runtime from the JAR file.
 * Only migrations that are older then the last migration date are executed.
 */
public class MigrationM {

    public static final String PREFIX = "[Migration Manager] ";

    private ProgramInfo info;
    private Logger logger;

    private DatabaseManager databaseManager;

    private LocalDateTime lastMigration = LocalDateTime.of(2020, 1, 1, 1, 1);
    private LocalDateTime oldestMigration = LocalDateTime.of(2020, 1, 1, 1, 1);

    public List<Migration> migrations = new ArrayList<>();

    public MigrationM(ProgramInfo info, DatabaseManager databaseManager) {
        this.info = info;
        this.logger = info.getLogger();
        this.databaseManager = databaseManager;
    }

    public MigrationM(String pluginName, String version, Logger logger, DatabaseManager databaseManager) {
        this(new ProgramInfo(pluginName, version, logger), databaseManager);
    }

    /**
     * Executes the migrations that have not been executed yet.
     * Please not that this process is synced.
     *
     * @return True if the migration was successfully executed.
     */
    public boolean migrate() {
        if (!databaseManager.isSetup()) {
            logger.info(PREFIX + "No existing migration structure detected.");
            logger.info(PREFIX + "Creating one now...");
            databaseManager.setup();
        }

        List<Migration> requiredMigrations = getRequiredMigrations();

        if (requiredMigrations.size() == 0) {
            logger.info(PREFIX + "Found no new migrations.");
            return true;
        }

        logger.info(String.format(PREFIX + "Found %s migrations. Executing now..", requiredMigrations.size()));
        long time = System.currentTimeMillis();

        int i = 1;
        for(Migration migration : requiredMigrations) {
            logger.info(String.format(PREFIX + "Executing migration (%s/%s) : %s", i, requiredMigrations.size(), migration.getName()));

            try {
                databaseManager.executeMigration(info, migration);
            } catch (Exception ex) {
                logger.warning(String.format(PREFIX + "An error occurred while applying the migration %s.", migration.getName()));
                logger.warning(String.format(PREFIX + "Exception: %s", ex.getMessage()));
                ex.printStackTrace();
                logger.warning(PREFIX + "Aborting migrations...");
                return false;
            }

            i++;
        }

        time = System.currentTimeMillis() - time;

        logger.info(String.format(PREFIX + "Migrations executed successfully. (Took: %.3f seconds)", ((float) time) / 1000));
        return true;
    }

    /**
     * Returns a list of migrations that need to be executed.
     * @return A sorted list of migrations that need to be excuted.
     */
    List<Migration> getRequiredMigrations() {
        lastMigration = databaseManager.lastMigration(info);

        // If there has never been a migration this could return null.
        // In that case default to an old date.
        if (lastMigration == null) {
            lastMigration = LocalDateTime.of(2000, 1, 1, 1, 1);
        }

        List<Migration> required = migrations.stream()
                .filter(migration -> migration.getDate().isAfter(lastMigration))
                .collect(Collectors.toList());

        required.sort(Comparator.comparing(Migration::getDate));

        return required;
    }

    /**
     * Adds a migration to the migration manager.
     * The filename will be used to determine the order of execution!
     * @param file The original file that should be scanned.
     * @param streamProvider An interface that provides a way of getting the stream from the file.
     */
    public void addMigration(File file, InputStreamProvider streamProvider) {
        this.addMigration(file.getName(), streamProvider);
    }

    /**
     * Adds a migration to the migration manager.
     * The actual file name is import for the order of the migrations!
     * @param fileName The full file name
     * @param streamProvider The StreamProvider to access the migration
     */
    public void addMigration(String fileName, InputStreamProvider streamProvider) {
        // Parse the date - Regex used to remove the extension.
        // Expected format: yyyy-M-d-HH.mm-description.extension
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d-HH.mm");

        int cutOff = fileName.indexOf(".");
        String dateString = fileName.substring(0, cutOff + 3);

        LocalDateTime date = LocalDateTime.parse(dateString, formatter);

        // Updated the oldest migration that has been found yet.
        if (date.isAfter(oldestMigration)) {
            oldestMigration = date;
        }

        migrations.add(new Migration(date, fileName, streamProvider));
    }

    /**
     * Loads all the migrations from the specified path in the resource folder.
     * @param folder The folder that should be loaded.
     * @param mainClass The main class that can be used to reach the resources in the jar.
     */
    public void loadMigrations(String folder, Class mainClass) {
        long time = System.currentTimeMillis();

        /*
            The following piece of code will extract the migrations from the folder specified.
            The difficult part is the fact that the jar file is a zipped file. Thus it requires some
            tricks to get the right file names.

            Credits for the code: https://stackoverflow.com/users/3474/erickson
            Stackoverflow issue: https://stackoverflow.com/questions/1429172/how-to-list-the-files-inside-a-jar-file
         */

        List<String> files = new ArrayList<>();

        CodeSource src = mainClass.getProtectionDomain().getCodeSource();
        if (src != null) {
            try {
                URL jar = src.getLocation();
                ZipInputStream zip = new ZipInputStream(jar.openStream());
                while (true) {
                    ZipEntry e = zip.getNextEntry();
                    if (e == null)
                        break;
                    String name = e.getName();
                    if (name.startsWith(folder + "/")) {
                        if (name.equals(folder + "/")) {
                            continue;
                        }
                        files.add(name);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (files.size() == 0) {
            logger.warning(String.format(PREFIX + "No migrations were added while searching the directory: %s", folder));
            return;
        }

        for (String fileName : files) {
            addMigration(fileName.substring(folder.length() + 1), () -> mainClass.getClassLoader().getResourceAsStream(fileName));
        }

        time = System.currentTimeMillis() - time;
        logger.info(String.format(PREFIX + "Locating the migrations took %.3f seconds", ((float) time) / 1000));
    }

    /**
     * Removes all files from migration manager.
     * This is not required.
     */
    public void flush() {
        migrations.clear();
    }
}
