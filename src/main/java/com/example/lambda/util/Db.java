package com.example.lambda.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection utility class
 */
public class Db {
    private static final Logger logger = LoggerFactory.getLogger(Db.class);
    
    private static final String DB_URL_ENV = "DB_URL";
    private static final String DB_USER_ENV = "DB_USER";
    private static final String DB_PASSWORD_ENV = "DB_PASSWORD";
    
    // Default values for local development
    private static final String DEFAULT_DB_URL = "jdbc:mysql://ai-game.cfkuy6mi4nng.ap-southeast-2.rds.amazonaws.com:3306/ai-game?useSSL=true&serverTimezone=UTC&characterEncoding=utf8";
    private static final String DEFAULT_DB_USER = "chenghao";
    
    /**
     * Get database connection from environment variables
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        String dbUrl = System.getenv(DB_URL_ENV);
        String dbUser = System.getenv(DB_USER_ENV);
        String dbPassword = System.getenv(DB_PASSWORD_ENV);
        
        // Use default values if environment variables are not set
        if (dbUrl == null || dbUrl.isEmpty()) {
            dbUrl = DEFAULT_DB_URL;
            logger.info("Using default DB_URL: {}", dbUrl);
        }
        
        if (dbUser == null || dbUser.isEmpty()) {
            dbUser = DEFAULT_DB_USER;
            logger.info("Using default DB_USER: {}", dbUser);
        }
        
        if (dbPassword == null || dbPassword.isEmpty()) {
            throw new SQLException("DB_PASSWORD environment variable is required");
        }
        
        try {
            logger.debug("Connecting to database: {} with user: {}", dbUrl, dbUser);
            Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            logger.debug("Database connection established successfully");
            return connection;
        } catch (SQLException e) {
            logger.error("Failed to connect to database: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Test database connection
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            logger.info("Database connection test successful");
            return true;
        } catch (SQLException e) {
            logger.error("Database connection test failed: {}", e.getMessage());
            return false;
        }
    }
}
