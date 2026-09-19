package me.lumen.comprehensiveEconomy.utils;

import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

public class DataStorage {
    private final Connection connection = connect();

    /**
     * Runs something in the database, all async
     * @param task the task to run, such as INSERT or SELECT
     */
    public void runInDatabase(DatabaseTask task) {
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () ->
                {
                    try {
                        task.run(connection);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    /**
     * Loads a sql file, as I think it is cleaner to have different code languages separate
     * @param path the path, from the resources folder
     * @return a SQL command string to execute
     * @throws FileNotFoundException if the file is not found
     */
    public final String loadSqlFile(String path) throws FileNotFoundException {
        InputStream is = getPlugin().getResource(path);
        if (is == null) {
            throw new FileNotFoundException("Resource not found: " + path);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sql = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }

            return sql.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final void createTables(){
        runInDatabase((connection) -> {
            Statement statement = connection.createStatement();
            for (String table : getTableStatements()) {
                statement.addBatch(table);
            }
            statement.executeBatch();
        });
    }

    private @NotNull Plugin getPlugin() {
        return ComprehensiveEconomy.getPlugin();
    }

    private @NotNull Collection<String> getTableStatements() {
        try {
            String homesTable = loadSqlFile("sql/homes/createHomesTable.sql");
            String econTable = loadSqlFile("sql/economy/create.sql");
            String bountiesTable = loadSqlFile("sql/bounty/createTable.sql");
            return List.of(homesTable, econTable, bountiesTable);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static DataStorage instance;
    public static DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    public @NonNull Connection connect() {
        try {
            File file = new File(ComprehensiveEconomy.getPlugin().getDataFolder(), "data.db");
            return DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
