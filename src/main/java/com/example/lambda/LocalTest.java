package com.example.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

/**
 * Local test class for testing Lambda function
 */
public class LocalTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static void main(String[] args) {
        try {
            // Set environment variables
            System.setProperty("DB_URL", "jdbc:mysql://ai-game.cfkuy6mi4nng.ap-southeast-2.rds.amazonaws.com:3306/ai-game?useSSL=true&serverTimezone=UTC&characterEncoding=utf8");
            System.setProperty("DB_USER", "chenghao");
            System.setProperty("DB_PASSWORD", "C1h2E3n4G5^&");
            
            UserHandler handler = new UserHandler();
            
            // Test 1: Create User
            System.out.println("=== Testing Create User ===");
            APIGatewayV2HTTPEvent createEvent = new APIGatewayV2HTTPEvent();
            createEvent.setRawPath("/users");
            createEvent.setBody("{\"username\":\"test_user_2\",\"email\":\"test2@example.com\",\"password\":\"password123\",\"score\":100,\"wallet_address\":\"0x1234567890abcdef\",\"wallet_type\":\"ETH\"}");
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            createEvent.setHeaders(headers);
            
            // Set request context for HTTP method
            APIGatewayV2HTTPEvent.RequestContext createRequestContext = new APIGatewayV2HTTPEvent.RequestContext();
            APIGatewayV2HTTPEvent.RequestContext.Http createHttp = new APIGatewayV2HTTPEvent.RequestContext.Http();
            createHttp.setMethod("POST");
            createRequestContext.setHttp(createHttp);
            createEvent.setRequestContext(createRequestContext);
            
            APIGatewayV2HTTPResponse createResponse = handler.handleRequest(createEvent, null);
            System.out.println("Status Code: " + createResponse.getStatusCode());
            System.out.println("Response: " + createResponse.getBody());
            System.out.println();
            
            // Test 2: List Users
            System.out.println("=== Testing List Users ===");
            APIGatewayV2HTTPEvent listEvent = new APIGatewayV2HTTPEvent();
            listEvent.setRawPath("/users");
            
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("limit", "5");
            queryParams.put("offset", "0");
            listEvent.setQueryStringParameters(queryParams);
            listEvent.setHeaders(headers);
            
            // Set request context for HTTP method
            APIGatewayV2HTTPEvent.RequestContext listRequestContext = new APIGatewayV2HTTPEvent.RequestContext();
            APIGatewayV2HTTPEvent.RequestContext.Http listHttp = new APIGatewayV2HTTPEvent.RequestContext.Http();
            listHttp.setMethod("GET");
            listRequestContext.setHttp(listHttp);
            listEvent.setRequestContext(listRequestContext);
            
            APIGatewayV2HTTPResponse listResponse = handler.handleRequest(listEvent, null);
            System.out.println("Status Code: " + listResponse.getStatusCode());
            System.out.println("Response: " + listResponse.getBody());
            System.out.println();
            
            // Test 3: Get User by ID
            System.out.println("=== Testing Get User by ID ===");
            APIGatewayV2HTTPEvent getEvent = new APIGatewayV2HTTPEvent();
            getEvent.setRawPath("/users/1");
            getEvent.setHeaders(headers);
            
            // Set request context for HTTP method
            APIGatewayV2HTTPEvent.RequestContext getRequestContext = new APIGatewayV2HTTPEvent.RequestContext();
            APIGatewayV2HTTPEvent.RequestContext.Http getHttp = new APIGatewayV2HTTPEvent.RequestContext.Http();
            getHttp.setMethod("GET");
            getRequestContext.setHttp(getHttp);
            getEvent.setRequestContext(getRequestContext);
            
            APIGatewayV2HTTPResponse getResponse = handler.handleRequest(getEvent, null);
            System.out.println("Status Code: " + getResponse.getStatusCode());
            System.out.println("Response: " + getResponse.getBody());
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Error during testing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
