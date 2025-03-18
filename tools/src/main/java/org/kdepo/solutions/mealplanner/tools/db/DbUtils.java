package org.kdepo.solutions.mealplanner.tools.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DbUtils {

    public static void closeQuietly(Connection conn) {
        try {
            close(conn);
        } catch (SQLException e) {
            System.out.println("[DB] Connection closing error");
            e.printStackTrace();
        }
    }

    public static void closeQuietly(Statement stmt) {
        try {
            close(stmt);
        } catch (SQLException e) {
            System.out.println("[DB] Statement closing error");
            e.printStackTrace();
        }
    }

    public static void close(Connection conn) throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    public static void close(Statement stmt) throws SQLException {
        if (stmt != null) {
            stmt.close();
        }
    }
}
