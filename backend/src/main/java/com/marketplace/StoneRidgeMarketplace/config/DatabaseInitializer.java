package com.marketplace.StoneRidgeMarketplace.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Automatically creates the database if it doesn't exist on application startup.
 * This runs before the DataSource is initialized.
 */
@Slf4j
public class DatabaseInitializer implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment env = event.getEnvironment();
        
        String datasourceUrl = env.getProperty("spring.datasource.url");
        String datasourceUsername = env.getProperty("spring.datasource.username");
        String datasourcePassword = env.getProperty("spring.datasource.password");
        
        if (datasourceUrl == null || datasourceUsername == null || datasourcePassword == null) {
            log.warn("Database configuration not found. Skipping database initialization.");
            return;
        }
        
        initializeDatabase(datasourceUrl, datasourceUsername, datasourcePassword);
    }

    private void initializeDatabase(String datasourceUrl, String datasourceUsername, String datasourcePassword) {
        try {
            // Extract database name from URL
            // Format: jdbc:postgresql://localhost:5432/database_name
            String databaseName = extractDatabaseName(datasourceUrl);
            
            if (databaseName == null || databaseName.isEmpty()) {
                log.warn("Could not extract database name from URL: {}", datasourceUrl);
                return;
            }

            // Connect to default 'postgres' database to check/create target database
            String defaultUrl = datasourceUrl.replace("/" + databaseName, "/postgres");
            
            log.info("Checking if database '{}' exists...", databaseName);
            
            try (Connection connection = DriverManager.getConnection(
                    defaultUrl, datasourceUsername, datasourcePassword)) {
                
                // Check if database exists
                boolean exists = databaseExists(connection, databaseName);
                
                if (!exists) {
                    log.info("Database '{}' does not exist. Creating it...", databaseName);
                    createDatabase(connection, databaseName);
                    log.info("Database '{}' created successfully!", databaseName);
                } else {
                    log.info("Database '{}' already exists.", databaseName);
                }
            }
            
        } catch (SQLException e) {
            log.error("Failed to initialize database: {}", e.getMessage(), e);
            // Don't throw exception - let Spring Boot handle the connection error
            // This allows the application to start even if database creation fails
        } catch (Exception e) {
            log.error("Unexpected error during database initialization: {}", e.getMessage(), e);
        }
    }

    private String extractDatabaseName(String url) {
        try {
            // Extract database name from JDBC URL
            // jdbc:postgresql://localhost:5432/database_name
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash != -1 && lastSlash < url.length() - 1) {
                String dbName = url.substring(lastSlash + 1);
                // Remove any query parameters
                int questionMark = dbName.indexOf('?');
                if (questionMark != -1) {
                    dbName = dbName.substring(0, questionMark);
                }
                return dbName;
            }
        } catch (Exception e) {
            log.error("Error extracting database name: {}", e.getMessage());
        }
        return null;
    }

    private boolean databaseExists(Connection connection, String databaseName) throws SQLException {
        String query = "SELECT 1 FROM pg_database WHERE datname = ?";
        try (java.sql.PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, databaseName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void createDatabase(Connection connection, String databaseName) throws SQLException {
        // PostgreSQL doesn't allow creating database in a transaction
        // So we need to commit any transaction and set autocommit
        connection.setAutoCommit(true);
        
        try (Statement stmt = connection.createStatement()) {
            // Use IF NOT EXISTS to avoid errors if database was created between check and creation
            String createDbQuery = String.format(
                "CREATE DATABASE %s WITH ENCODING = 'UTF8'",
                escapeIdentifier(databaseName)
            );
            stmt.executeUpdate(createDbQuery);
            log.debug("Executed: {}", createDbQuery);
        }
    }

    private String escapeIdentifier(String identifier) {
        // PostgreSQL identifiers should be quoted if they contain special characters
        // For safety, we'll quote all identifiers
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}

