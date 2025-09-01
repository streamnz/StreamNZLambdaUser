package com.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.example.lambda.dao.UserDao;
import com.example.lambda.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main Lambda handler for user CRUD operations
 */
public class UserHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    
    private static final Pattern USER_ID_PATTERN = Pattern.compile("/users/(\\d+)");
    private static final UserDao userDao = new UserDao();
    
    // Default pagination values
    private static final int DEFAULT_LIMIT = 10;
    private static final int DEFAULT_OFFSET = 0;
    private static final int MAX_LIMIT = 100;

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        long startTime = System.currentTimeMillis();
        String httpMethod = event.getRequestContext().getHttp().getMethod();
        String path = event.getRawPath();
        
        // Add debug logging to see what API Gateway is sending
        logger.info("=== API Gateway Event Debug ===");
        logger.info("HTTP Method: {}", httpMethod);
        logger.info("Path: {}", path);
        logger.info("Raw Path: {}", event.getRawPath());
        logger.info("Path Parameters: {}", event.getPathParameters());
        logger.info("Query String Parameters: {}", event.getQueryStringParameters());
        logger.info("Headers: {}", event.getHeaders());
        logger.info("Body: {}", event.getBody());
        logger.info("================================");
        
        // Add null checks
        if (httpMethod == null) {
            httpMethod = "GET";
        }
        if (path == null) {
            path = "/";
        }
        
        logger.info("Received request: {} {}, path: {}", httpMethod, path, path);
        
        try {
            // Route the request based on HTTP method and path
            APIGatewayV2HTTPResponse response = routeRequest(httpMethod, path, event);
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("Request completed: {} {} - Status: {}, Duration: {}ms", 
                       httpMethod, path, response.getStatusCode(), duration);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error processing request: {} {} - {}", httpMethod, path, e.getMessage(), e);
            return createErrorResponse(500, "Internal server error: " + e.getMessage());
        }
    }
    
    /**
     * Route the request to appropriate handler based on HTTP method and path
     */
    private APIGatewayV2HTTPResponse routeRequest(String httpMethod, String path, APIGatewayV2HTTPEvent event) {
        // Add null checks
        if (httpMethod == null) {
            httpMethod = "GET";
        }
        if (path == null) {
            path = "/";
        }
        
        switch (httpMethod) {
            case "POST":
                if ("/users".equals(path)) {
                    return handleCreateUser(event);
                }
                break;
                
            case "GET":
                if ("/users".equals(path)) {
                    return handleListUsers(event);
                } else {
                    Matcher matcher = USER_ID_PATTERN.matcher(path);
                    if (matcher.matches()) {
                        int userId = Integer.parseInt(matcher.group(1));
                        return handleGetUser(userId);
                    }
                }
                break;
                
            case "PUT":
                Matcher putMatcher = USER_ID_PATTERN.matcher(path);
                if (putMatcher.matches()) {
                    int userId = Integer.parseInt(putMatcher.group(1));
                    return handleUpdateUser(userId, event);
                }
                break;
                
            case "DELETE":
                Matcher deleteMatcher = USER_ID_PATTERN.matcher(path);
                if (deleteMatcher.matches()) {
                    int userId = Integer.parseInt(deleteMatcher.group(1));
                    return handleDeleteUser(userId);
                }
                break;
        }
        
        return createErrorResponse(404, "Endpoint not found: " + httpMethod + " " + path);
    }
    
    /**
     * Handle POST /users - Create new user
     */
    private APIGatewayV2HTTPResponse handleCreateUser(APIGatewayV2HTTPEvent event) {
        try {
            String body = event.getBody();
            if (body == null || body.trim().isEmpty()) {
                return createErrorResponse(400, "Request body is required");
            }
            
            User user = objectMapper.readValue(body, User.class);
            
            // Validate required fields
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return createErrorResponse(400, "Username is required");
            }
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return createErrorResponse(400, "Email is required");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return createErrorResponse(400, "Password is required");
            }
            
            User createdUser = userDao.create(user);
            return createSuccessResponse(201, createdUser);
            
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            return createErrorResponse(500, "Failed to create user: " + e.getMessage());
        }
    }
    
    /**
     * Handle GET /users/{id} - Get user by ID
     */
    private APIGatewayV2HTTPResponse handleGetUser(int userId) {
        try {
            User user = userDao.getById(userId);
            if (user == null) {
                return createErrorResponse(404, "User not found with ID: " + userId);
            }
            
            return createSuccessResponse(200, user);
            
        } catch (Exception e) {
            logger.error("Error getting user {}: {}", userId, e.getMessage(), e);
            return createErrorResponse(500, "Failed to get user: " + e.getMessage());
        }
    }
    
    /**
     * Handle PUT /users/{id} - Update user
     */
    private APIGatewayV2HTTPResponse handleUpdateUser(int userId, APIGatewayV2HTTPEvent event) {
        try {
            String body = event.getBody();
            if (body == null || body.trim().isEmpty()) {
                return createErrorResponse(400, "Request body is required");
            }
            
            User user = objectMapper.readValue(body, User.class);
            
            // Validate required fields
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return createErrorResponse(400, "Username is required");
            }
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return createErrorResponse(400, "Email is required");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return createErrorResponse(400, "Password is required");
            }
            
            boolean updated = userDao.update(userId, user);
            if (!updated) {
                return createErrorResponse(404, "User not found with ID: " + userId);
            }
            
            // Get the updated user
            User updatedUser = userDao.getById(userId);
            return createSuccessResponse(200, updatedUser);
            
        } catch (Exception e) {
            logger.error("Error updating user {}: {}", userId, e.getMessage(), e);
            return createErrorResponse(500, "Failed to update user: " + e.getMessage());
        }
    }
    
    /**
     * Handle DELETE /users/{id} - Delete user
     */
    private APIGatewayV2HTTPResponse handleDeleteUser(int userId) {
        try {
            boolean deleted = userDao.delete(userId);
            if (!deleted) {
                return createErrorResponse(404, "User not found with ID: " + userId);
            }
            
            return createSuccessResponse(200, Map.of("message", "User deleted successfully"));
            
        } catch (Exception e) {
            logger.error("Error deleting user {}: {}", userId, e.getMessage(), e);
            return createErrorResponse(500, "Failed to delete user: " + e.getMessage());
        }
    }
    
    /**
     * Handle GET /users - List users with pagination
     */
    private APIGatewayV2HTTPResponse handleListUsers(APIGatewayV2HTTPEvent event) {
        try {
            // Parse query parameters
            Map<String, String> queryParams = event.getQueryStringParameters();
            int limit = DEFAULT_LIMIT;
            int offset = DEFAULT_OFFSET;
            
            if (queryParams != null) {
                if (queryParams.containsKey("limit")) {
                    try {
                        limit = Integer.parseInt(queryParams.get("limit"));
                        if (limit <= 0 || limit > MAX_LIMIT) {
                            return createErrorResponse(400, "Limit must be between 1 and " + MAX_LIMIT);
                        }
                    } catch (NumberFormatException e) {
                        return createErrorResponse(400, "Invalid limit parameter");
                    }
                }
                
                if (queryParams.containsKey("offset")) {
                    try {
                        offset = Integer.parseInt(queryParams.get("offset"));
                        if (offset < 0) {
                            return createErrorResponse(400, "Offset must be non-negative");
                        }
                    } catch (NumberFormatException e) {
                        return createErrorResponse(400, "Invalid offset parameter");
                    }
                }
            }
            
            List<User> users = userDao.list(limit, offset);
            int totalCount = userDao.getCount();
            
            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            response.put("pagination", Map.of(
                "limit", limit,
                "offset", offset,
                "total", totalCount,
                "hasMore", (offset + limit) < totalCount
            ));
            
            return createSuccessResponse(200, response);
            
        } catch (Exception e) {
            logger.error("Error listing users: {}", e.getMessage(), e);
            return createErrorResponse(500, "Failed to list users: " + e.getMessage());
        }
    }
    
    /**
     * Create a success response
     */
    private APIGatewayV2HTTPResponse createSuccessResponse(int statusCode, Object data) {
        try {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", true);
            responseBody.put("data", data);
            
            String jsonResponse = objectMapper.writeValueAsString(responseBody);
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(statusCode)
                    .withHeaders(headers)
                    .withBody(jsonResponse)
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error creating success response: {}", e.getMessage(), e);
            return createErrorResponse(500, "Internal server error");
        }
    }
    
    /**
     * Create an error response
     */
    private APIGatewayV2HTTPResponse createErrorResponse(int statusCode, String errorMessage) {
        try {
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("success", false);
            responseBody.put("error", errorMessage);
            
            String jsonResponse = objectMapper.writeValueAsString(responseBody);
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(statusCode)
                    .withHeaders(headers)
                    .withBody(jsonResponse)
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error creating error response: {}", e.getMessage(), e);
            // Fallback to simple text response
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(statusCode)
                    .withHeaders(headers)
                    .withBody("{\"success\":false,\"error\":\"Internal server error\"}")
                    .build();
        }
    }
}
