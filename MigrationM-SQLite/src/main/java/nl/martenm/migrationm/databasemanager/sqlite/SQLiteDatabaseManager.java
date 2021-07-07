package nl.martenm.migrationm.databasemanager.sqlite;

import nl.martenm.migrationm.api.DatabaseManager;
import nl.martenm.migrationm.api.Migration;
import nl.martenm.migrationm.api.exceptions.MigrationException;
import nl.martenm.migrationm.util.ProgramInfo;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Scanner;

public class SQLiteDatabaseManager implements DatabaseManager {

    private String tableName = "migrations";
    private DataSource source;

    public SQLiteDatabaseManager(DataSource source) {
        this.source = source;
    }

    private boolean updateLastMigration(ProgramInfo info, LocalDateTime localDateTime) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    String.format("REPLACE INTO %s (program, version, last_migration) VALUES (?, ?, ?)", tableName));

            statement.setString(1, info.getName());
            statement.setString(2, info.getVersion());
            statement.setTimestamp(3, Timestamp.valueOf(localDateTime));

            statement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public LocalDateTime lastMigration(ProgramInfo info) {
        LocalDateTime dateTime = null;

        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    String.format("SELECT last_migration FROM %s WHERE program = ?", tableName));

            statement.setString(1, info.getName());

            ResultSet set = statement.executeQuery();
            if (set.next()) {
                dateTime = set.getTimestamp("last_migration").toLocalDateTime();
            }

        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }

        return dateTime;
    }

    @Override
    public boolean isSetup() {
        boolean tableExists = false;

        try (Connection connection = source.getConnection()) {

            PreparedStatement statement = connection.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name=?");
            statement.setString(1, tableName);

            ResultSet set = statement.executeQuery();
            if (set.next()) {
                tableExists = true;
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }

        return tableExists;
    }

    @Override
    public void setup() {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(String.format("CREATE TABLE %s (" +
                    "program VARCHAR(64) PRIMARY KEY," +
                    "version VARCHAR(12) NOT NULL," +
                    "last_migration TIMESTAMP NOT NULL" +
                    ");", tableName));

            statement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void executeMigration(ProgramInfo info, Migration migration) throws MigrationException {

        Scanner scanner = new Scanner(migration.getInputStream());
        scanner.useDelimiter(";");

        String sql = "No query loaded";

        Connection connection = null;
        try {

            /*
             * Execute the whole file, query per query.
             * Only if all rows succeed commit the changes.
             */

            connection = source.getConnection();
            connection.setAutoCommit(false);

            while(scanner.hasNext()) {
                sql = scanner.next();
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.execute();
            }

            connection.commit();

        } catch (SQLException ex) {
            // Abort the the transaction and close the connection.
            if (connection != null) {
                try {
                    connection.rollback();
                    connection.close();
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
            }

            throw new MigrationException("Failed to execute query SQLite: " + sql + " Original message: " + ex.getMessage(), ex);
        }


        updateLastMigration(info, migration.getDate());
    }
}
