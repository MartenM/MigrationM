package nl.martenm.migrationm.impl;

import nl.martenm.migrationm.api.DatabaseManager;
import nl.martenm.migrationm.api.Migration;
import nl.martenm.migrationm.util.ProgramInfo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class TestDatabase implements DatabaseManager {

    private Map<String, LocalDateTime> latestMigrations = new HashMap<>();
    private boolean setup = false;

    @Override
    public LocalDateTime lastMigration(ProgramInfo info) {
        return latestMigrations.get(info.getName());
    }

    @Override
    public boolean isSetup() {
        return this.setup;
    }

    @Override
    public void setup() {
        this.setup = true;
    }

    @Override
    public void executeMigration(ProgramInfo info, Migration migration) {
        this.latestMigrations.put(info.getName(), migration.getDate());
    }

    public void setLatestMigration(ProgramInfo info, LocalDateTime latestMigration) {
        this.latestMigrations.put(info.getName(), latestMigration);
    }
}
