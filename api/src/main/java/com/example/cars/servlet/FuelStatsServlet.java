package com.example.cars.servlet;

import com.example.cars.dto.FuelStatsResponse;
import com.example.cars.dto.Response;
import com.example.cars.service.FuelEntryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

// Manual Java Servlet for GET /servlet/fuel-stats?carId={id} - demonstrates request lifecycle handling
public class FuelStatsServlet extends HttpServlet {
    
    private final FuelEntryService fuelEntryService;
    private final ObjectMapper objectMapper;
    
    public FuelStatsServlet(FuelEntryService fuelEntryService) {
        this.fuelEntryService = fuelEntryService;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        // Manually parse carId from query parameters
        String queryString = request.getQueryString();
        Long carId = null;
        boolean carIdProvided = false;
        String carIdValue = null;
        
        if (queryString != null && !queryString.isEmpty()) {
            // Parse query string manually
            String[] params = queryString.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length == 2 && "carId".equals(keyValue[0])) {
                    carIdProvided = true;
                    carIdValue = keyValue[1];
                    try {
                        carId = Long.parseLong(carIdValue);
                        break;
                    } catch (NumberFormatException e) {
                        // Invalid carId format - will be handled below
                        break;
                    }
                }
            }
        }
        
        // Validate carId parameter
        if (!carIdProvided) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                "carId parameter is required");
            return;
        }
        
        if (carId == null) {
            // Parameter was provided but is not a valid number
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                "carId must be a valid number. Received: '" + carIdValue + "'");
            return;
        }
        
        try {
            // Use the same Service layer instance as the REST API
            Map<String, Double> stats = fuelEntryService.getFuelStats(carId);
            
            // Build response object
            FuelStatsResponse statsResponse = new FuelStatsResponse();
            statsResponse.totalLiters = stats.get("totalLiters");
            statsResponse.totalPrice = stats.get("totalPrice");
            statsResponse.avgPer100km = stats.get("avgPer100km");
            
            // Wrap in Response wrapper for consistency with REST API
            Response<FuelStatsResponse> responseWrapper = Response.success(statsResponse);
            
            // Set status code explicitly (200 OK)
            response.setStatus(HttpServletResponse.SC_OK);
            // Set Content-Type explicitly
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            
            // Write JSON response
            PrintWriter writer = response.getWriter();
            String json = objectMapper.writeValueAsString(responseWrapper);
            writer.write(json);
            writer.flush();
            
        } catch (ResponseStatusException e) {
            // Handle Spring's ResponseStatusException from ValidationUtils
            int statusCode = e.getStatusCode().value();
            String message = e.getReason() != null ? e.getReason() : e.getMessage();
            sendErrorResponse(response, statusCode, message != null ? message : "An error occurred");
            
        } catch (IllegalArgumentException e) {
            // Service layer throws IllegalArgumentException for validation/not found errors
            String message = e.getMessage();
            int statusCode = (message != null && message.toLowerCase().contains("not found"))
                ? HttpServletResponse.SC_NOT_FOUND
                : HttpServletResponse.SC_BAD_REQUEST;
            sendErrorResponse(response, statusCode, message != null ? message : "An error occurred");
            
        } catch (Exception e) {
            // Internal server error
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred");
        }
    }
    
    // Sends error response with consistent JSON structure using Response wrapper (same as REST API)
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) 
            throws IOException {
        // Set status code explicitly
        response.setStatus(statusCode);
        // Set Content-Type explicitly
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Create error response using the same Response wrapper as REST API
        Response<Object> errorResponse = Response.error(message);
        PrintWriter writer = response.getWriter();
        objectMapper.writeValue(writer, errorResponse);
        writer.flush();
    }
}

