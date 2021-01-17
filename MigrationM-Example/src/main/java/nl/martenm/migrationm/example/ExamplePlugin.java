package nl.martenm.migrationm.example;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import nl.martenm.migrationm.MigrationM;
import nl.martenm.migrationm.util.ProgramInfo;
import nl.martenm.migrationm.databasemanager.sql.SQLDatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Example plugin showing the functions of the MigrationM migration manager.
 */
public class ExamplePlugin extends JavaPlugin {

    private MigrationM migrationManager = null;
    private MysqlDataSource dataSource = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        /*
            STEP 1:
            Setup the DataSource as usual.
            This can be any SQL datasource (HikariCP for example).
         */
        String address = getConfig().getString("address");
        int port = getConfig().getInt("port");
        String database = getConfig().getString("database");

        /*
            Don't hardcode your credentials dummies.
         */
        dataSource = new MysqlDataSource();
        dataSource.setServerName(address);
        dataSource.setUser(getConfig().getString("username"));
        dataSource.setPassword(getConfig().getString("password"));
        dataSource.setDatabaseName(database);
        dataSource.setPort(port);

        /*
            STEP 2:
            Create the MigrationM migration manager.
            Provide it the basic info about your plugin.

            Also create the DataBaseManager. In this case we will use one that knows how to work with SQL files.
         */
        migrationManager = new MigrationM(
                new ProgramInfo(getName(), getDescription().getVersion(), getLogger()),
                new SQLDatabaseManager(dataSource));

        /*
            STEP 3:
            Load all migrations located in the migrations folder.

            Note that you can also hardcode or get your migrations from somewhere else and
            add them using the migration #.addMigration(fileName, streamProvider) method.
         */
        migrationManager.loadMigrations("migrations", ExamplePlugin.class);

        /*
            STEP 4:
            Execute the migrations that are required!
            This will not execute migrations that have already been executed.

            Using the result of this function is not required but recommended.
         */
        boolean success = migrationManager.migrate();
        if(!success) {
            getLogger().warning("Cannot run the plugin if the migrations are not applied.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
