package com.example.lambda.dao;

import com.example.lambda.model.User;
import com.example.lambda.util.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User entity
 */
public class UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    
    /**
     * Create a new user
     * @param user User object to create
     * @return Created user with generated ID
     * @throws SQLException if database operation fails
     */
    public User create(User user) throws SQLException {
        long startTime = System.currentTimeMillis();
        logger.info("Creating user with email: {}", user.getEmail());
        
        String sql = "INSERT INTO users (username, email, password, score, wallet_address, wallet_type, bind_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setObject(4, user.getScore());
            stmt.setString(5, user.getWalletAddress());
            stmt.setString(6, user.getWalletType());
            stmt.setObject(7, user.getBindTime());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                    logger.info("User created successfully with ID: {}, took {}ms", user.getId(), System.currentTimeMillis() - startTime);
                    return user;
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }
    
    /**
     * Get user by ID
     * @param id User ID
     * @return User object or null if not found
     * @throws SQLException if database operation fails
     */
    public User getById(int id) throws SQLException {
        long startTime = System.currentTimeMillis();
        logger.info("Getting user by ID: {}", id);
        
        String sql = "SELECT id, username, email, password, score, wallet_address, wallet_type, " +
                     "bind_time FROM users WHERE id = ?";
        
        try (Connection conn = Db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = mapResultSetToUser(rs);
                    logger.info("User found with ID: {}, took {}ms", id, System.currentTimeMillis() - startTime);
                    return user;
                } else {
                    logger.info("User not found with ID: {}, took {}ms", id, System.currentTimeMillis() - startTime);
                    return null;
                }
            }
        }
    }
    
    /**
     * Update user by ID
     * @param id User ID
     * @param user Updated user data
     * @return true if updated, false if not found
     * @throws SQLException if database operation fails
     */
    public boolean update(int id, User user) throws SQLException {
        long startTime = System.currentTimeMillis();
        logger.info("Updating user with ID: {}", id);
        
        String sql = "UPDATE users SET username = ?, email = ?, password = ?, score = ?, " +
                     "wallet_address = ?, wallet_type = ?, bind_time = ? WHERE id = ?";
        
        try (Connection conn = Db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setObject(4, user.getScore());
            stmt.setString(5, user.getWalletAddress());
            stmt.setString(6, user.getWalletType());
            stmt.setObject(7, user.getBindTime());
            stmt.setInt(8, id);
            
            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;
            logger.info("User update result: {}, took {}ms", updated ? "success" : "not found", System.currentTimeMillis() - startTime);
            return updated;
        }
    }
    
    /**
     * Delete user by ID
     * @param id User ID
     * @return true if deleted, false if not found
     * @throws SQLException if database operation fails
     */
    public boolean delete(int id) throws SQLException {
        long startTime = System.currentTimeMillis();
        logger.info("Deleting user with ID: {}", id);
        
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = Db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            boolean deleted = affectedRows > 0;
            logger.info("User deletion result: {}, took {}ms", deleted ? "success" : "not found", System.currentTimeMillis() - startTime);
            return deleted;
        }
    }
    
    /**
     * List users with pagination
     * @param limit Maximum number of records to return
     * @param offset Number of records to skip
     * @return List of users
     * @throws SQLException if database operation fails
     */
    public List<User> list(int limit, int offset) throws SQLException {
        long startTime = System.currentTimeMillis();
        logger.info("Listing users with limit: {}, offset: {}", limit, offset);
        
        String sql = "SELECT id, username, email, password, score, wallet_address, wallet_type, " +
                     "bind_time FROM users ORDER BY id LIMIT ? OFFSET ?";
        
        try (Connection conn = Db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
                logger.info("Found {} users, took {}ms", users.size(), System.currentTimeMillis() - startTime);
                return users;
            }
        }
    }
    
    /**
     * Get total count of users
     * @return Total number of users
     * @throws SQLException if database operation fails
     */
    public int getCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        
        try (Connection conn = Db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
    
    /**
     * Map ResultSet to User object
     * @param rs ResultSet
     * @return User object
     * @throws SQLException if mapping fails
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setScore(rs.getObject("score", Integer.class));
        user.setWalletAddress(rs.getString("wallet_address"));
        user.setWalletType(rs.getString("wallet_type"));
        
        Timestamp bindTime = rs.getTimestamp("bind_time");
        if (bindTime != null) {
            user.setBindTime(bindTime.toLocalDateTime());
        }
        

        
        return user;
    }
}
