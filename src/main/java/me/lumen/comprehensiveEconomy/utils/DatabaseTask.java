package me.lumen.comprehensiveEconomy.utils;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseTask {
    void run(Connection connection) throws SQLException;
}
