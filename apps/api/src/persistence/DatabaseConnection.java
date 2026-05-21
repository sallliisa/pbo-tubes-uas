package persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3306/consulting_company_uas";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "gamer42069";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("DB_URL", DEFAULT_DB_URL);
        String user = System.getenv().getOrDefault("DB_USER", DEFAULT_DB_USER);
        String password = System.getenv().getOrDefault("DB_PASSWORD", DEFAULT_DB_PASSWORD);
        return DriverManager.getConnection(url, user, password);
    }
}
