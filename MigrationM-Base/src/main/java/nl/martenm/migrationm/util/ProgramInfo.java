package nl.martenm.migrationm.util;

import java.util.logging.Logger;

/**
 * Stores basic information about the application like the name and it's version.
 */
public class ProgramInfo {

    private final String name;
    private final String version;
    private final Logger logger;

    /**
     *
     * @param name The program name.
     * @param version The version, no specific format required.
     * @param logger The logger used for logging.
     */
    public ProgramInfo(String name, String version, Logger logger) {
        this.name = name;
        this.version = version;
        this.logger = logger;
    }

    /**
     * Gets the program name, provided by the program.
     * Please note that a program can create different MigrationM instances if required.
     * @return The program name provided by the program
     */
    public String getName() {
        return name;
    }

    /**
     * The version provided by the program.
     * @return The version as a string
     */
    public String getVersion() {
        return version;
    }

    /**
     * The logger provided by the program.
     * @return The logger
     */
    public Logger getLogger() {
        return logger;
    }
}
