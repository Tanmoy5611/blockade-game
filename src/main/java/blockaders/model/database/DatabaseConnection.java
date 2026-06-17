package blockaders.model.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {
    // Local database config file
    private static final Path CONFIG_FILE = Path.of("config", "database.properties");

    // Environment variable for database url
    private static final String DB_URL_ENV = "BLOCKADE_DB_URL";

    // Environment variable for database username
    private static final String DB_USER_ENV = "BLOCKADE_DB_USER";

    // Environment variable for database password
    private static final String DB_PASSWORD_ENV = "BLOCKADE_DB_PASSWORD";

    // Properties key for database url
    private static final String DB_URL_PROPERTY = "db.url";

    // Properties key for database username
    private static final String DB_USER_PROPERTY = "db.user";

    // Properties key for database password
    private static final String DB_PASSWORD_PROPERTY = "db.password";

    // Message shown when database settings are missing
    private static final String MISSING_CONFIG_MESSAGE =
            "Database is not configured. Create config/database.properties or set BLOCKADE_DB_URL, BLOCKADE_DB_USER, and BLOCKADE_DB_PASSWORD";

    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    private static Connection connection = null;

    static {
        Properties properties = loadProperties();
        DB_URL = firstValue(System.getenv(DB_URL_ENV), properties.getProperty(DB_URL_PROPERTY));
        DB_USER = firstValue(System.getenv(DB_USER_ENV), properties.getProperty(DB_USER_PROPERTY));
        DB_PASSWORD = firstValue(System.getenv(DB_PASSWORD_ENV), properties.getProperty(DB_PASSWORD_PROPERTY));
    }

    // Prevents creating utility class objects
    private DatabaseConnection() {
    }

    // Returns an open database connection
    public static Connection getConnection() throws SQLException {
        if (!isConfigured()) {
            throw new SQLException(MISSING_CONFIG_MESSAGE);
        }
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
        return connection;
    }

    // Checks whether all database settings are available
    private static boolean isConfigured() {
        return isPresent(DB_URL) && isPresent(DB_USER) && isPresent(DB_PASSWORD);
    }

    // Loads database properties from the local config file
    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (var input = Files.newInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException ignored) {
            // Login shows database errors when configuration is missing
        }
        return properties;
    }

    // Returns the first value when it exists otherwise returns the second value
    private static String firstValue(String first, String second) {
        return isPresent(first) ? first : second;
    }

    // Checks whether a string has usable text
    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}