package nl.martenm.migrationm.databasemanager.sql;

import nl.martenm.migrationm.api.DatabaseManager;
import nl.martenm.migrationm.api.Migration;
import nl.martenm.migrationm.util.ProgramInfo;
import nl.martenm.migrationm.api.exceptions.MigrationException;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Used to keep track of migrations that apply to an SQL database using a SQL datasource.
 *
 * Tables:
 *   migrations
 *      program (VARCHAR) (KEY) - version (VARCHAR(12)) - last_migration LONG
 */
public class SQLDatabaseManager implements DatabaseManager {

    private String tableName = "migrations";
    private DataSource source;

    public SQLDatabaseManager(DataSource source) {
        this.source = source;
    }

    public SQLDatabaseManager(DataSource source, String tableName) {
        this(source);
        this.tableName = tableName;
    }

    private boolean updateLastMigration(ProgramInfo info, LocalDateTime localDateTime) {
        try (Connection connection = source.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                    String.format("INSERT INTO %s (program, version, last_migration) VALUES (?, ?, ?)" +
                            " ON DUPLICATE KEY UPDATE version=?, last_migration=?", tableName));

            statement.setString(1, info.getName());
            statement.setString(2, info.getVersion());
            statement.setTimestamp(3, Timestamp.valueOf(localDateTime));

            statement.setString(4, info.getVersion());
            statement.setTimestamp(5, Timestamp.valueOf(localDateTime));

            statement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

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

    public boolean isSetup() {
        boolean tableExists = false;

        try (Connection connection = source.getConnection()) {

            PreparedStatement statement = connection.prepareStatement("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?");
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

            throw new MigrationException("Failed to execute query SQL: " + sql + " Original message: " + ex.getMessage(), ex);
        }


        updateLastMigration(info, migration.getDate());
    }
}
