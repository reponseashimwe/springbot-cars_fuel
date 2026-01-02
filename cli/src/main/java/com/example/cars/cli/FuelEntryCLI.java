package com.example.cars.cli;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class FuelEntryCLI {
    private static final String BASE_URL = "http://localhost:8080/api/fuel-entries";
    private static final String CARS_BASE_URL = "http://localhost:8080/api/cars";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Lists all fuel entries.
     */
    public static void listFuelEntries() throws IOException, InterruptedException {
        HttpResponse<String> response = CliUtils.get(BASE_URL);
        CliUtils.printListResponse(response);
    }

    /**
     * Gets a fuel entry by ID.
     */
    public static void getFuelEntry(String[] args) throws IOException, InterruptedException {
        Map<String, String> params = CliUtils.parseArgs(args);
        Long id = CliUtils.getLongParam(params, "id");

        if (id == null) {
            System.err.println("Error: --id parameter is required");
            System.exit(1);
        }

        HttpResponse<String> response = CliUtils.get(BASE_URL + "/" + id);
        CliUtils.printListResponse(response);
    }

    /**
     * Adds fuel entry for a car (car-specific endpoint).
     */
    public static void addFuel(String[] args) throws IOException, InterruptedException {
        Map<String, String> params = CliUtils.parseArgs(args);
        
        // Only check if parameters are provided, let API validate the values
        String carIdStr = CliUtils.getStringParam(params, "carId");
        String litersStr = CliUtils.getStringParam(params, "liters");
        String priceStr = CliUtils.getStringParam(params, "price");
        String odometerStr = CliUtils.getStringParam(params, "odometer");

        if (carIdStr == null || litersStr == null || priceStr == null || odometerStr == null) {
            StringBuilder errorMsg = new StringBuilder("Error: Missing required parameters:");
            if (carIdStr == null) {
                errorMsg.append(" --carId");
            }
            if (litersStr == null) {
                errorMsg.append(" --liters");
            }
            if (priceStr == null) {
                errorMsg.append(" --price");
            }
            if (odometerStr == null) {
                errorMsg.append(" --odometer");
            }
            System.err.println(errorMsg.toString());
            System.exit(1);
        }

        // Try to parse, but use raw string if parsing fails - API will validate
        Object liters = CliUtils.getDoubleOrStringParam(params, "liters");
        Object price = CliUtils.getDoubleOrStringParam(params, "price");
        Object odometer = CliUtils.getIntegerOrStringParam(params, "odometer");
        Long carId = CliUtils.getLongParam(params, "carId");
        
        if (carId == null) {
            // carId is needed for the URL path, so we need it to be valid
            System.err.println("Error: --carId must be a valid number");
            System.exit(1);
        }

        Map<String, Object> fuelEntryData = new HashMap<>();
        fuelEntryData.put("liters", liters);
        fuelEntryData.put("price", price);
        fuelEntryData.put("odometer", odometer);
        String json = CliUtils.buildJson(fuelEntryData);

        HttpResponse<String> response = CliUtils.post(CARS_BASE_URL + "/" + carId + "/fuel", json);
        CliUtils.printActionResponse(response);
    }

    /**
     * Gets all fuel entries for a specific car.
     */
    public static void getCarFuel(String[] args) throws IOException, InterruptedException {
        Map<String, String> params = CliUtils.parseArgs(args);
        Long carId = CliUtils.getLongParam(params, "carId");

        if (carId == null) {
            System.err.println("Error: --carId parameter is required");
            System.exit(1);
        }

        HttpResponse<String> response = CliUtils.get(CARS_BASE_URL + "/" + carId + "/fuel");
        CliUtils.printListResponse(response);
    }

    /**
     * Gets fuel statistics for a specific car and formats as text.
     */
    public static void fuelStats(String[] args) throws IOException, InterruptedException {
        Map<String, String> params = CliUtils.parseArgs(args);
        Long carId = CliUtils.getLongParam(params, "carId");

        if (carId == null) {
            System.err.println("Error: --carId parameter is required");
            System.exit(1);
        }

        HttpResponse<String> response = CliUtils.get(CARS_BASE_URL + "/" + carId + "/fuel/stats");
        
        if (response.statusCode() >= 400) {
            // Extract and display error message
            String errorMessage = CliUtils.extractErrorMessage(response.body());
            if (errorMessage != null) {
                System.err.println("ERROR: " + errorMessage);
            } else {
                System.err.println("ERROR: HTTP " + response.statusCode());
            }
            return;
        }

        // Parse JSON and format as text
        try {
            JsonNode responseNode = objectMapper.readTree(response.body());
            // Unwrap the Response object to get the data field
            JsonNode dataNode = responseNode.get("data");
            if (dataNode == null) {
                // Fallback if data field is missing
                CliUtils.printResponse(response);
                return;
            }
            
            double totalLiters = dataNode.get("totalLiters").asDouble();
            double totalPrice = dataNode.get("totalPrice").asDouble();
            double avgPer100km = dataNode.get("avgPer100km").asDouble();
            
            // Format output as specified
            System.out.println("Total fuel: " + formatLiters(totalLiters) + " L");
            System.out.println("Total cost: " + String.format("%.2f", totalPrice));
            System.out.println("Average consumption: " + formatConsumption(avgPer100km) + " L/100km");
        } catch (Exception e) {
            // Fallback to JSON if parsing fails
            CliUtils.printResponse(response);
        }
    }

    /**
     * Formats liters - whole number if integer, otherwise with decimals.
     */
    private static String formatLiters(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        // Remove trailing zeros
        return String.format("%.10f", value).replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    /**
     * Formats consumption - one decimal place.
     */
    private static String formatConsumption(double value) {
        return String.format("%.1f", value);
    }

    /**
     * Updates an existing fuel entry.
     */
    public static void updateFuelEntry(String[] args) throws IOException, InterruptedException {
        Map<String, String> params = CliUtils.parseArgs(args);
        
        // Only check if parameters are provided, let API validate the values
        String idStr = CliUtils.getStringParam(params, "id");
        String carIdStr = CliUtils.getStringParam(params, "carId");
        String litersStr = CliUtils.getStringParam(params, "liters");
        String priceStr = CliUtils.getStringParam(params, "price");
        String odometerStr = CliUtils.getStringParam(params, "odometer");

        if (idStr == null) {
            System.err.println("Error: --id parameter is required");
            System.exit(1);
        }
        if (carIdStr == null || litersStr == null || priceStr == null || odometerStr == null) {
            StringBuilder errorMsg = new StringBuilder("Error: Missing required parameters:");
            if (carIdStr == null) {
                errorMsg.append(" --carId");
            }
            if (litersStr == null) {
                errorMsg.append(" --liters");
            }
            if (priceStr == null) {
                errorMsg.append(" --price");
            }
            if (odometerStr == null) {
                errorMsg.append(" --odometer");
            }
            System.err.println(errorMsg.toString());
            System.exit(1);
        }

        // Try to parse, but use raw string if parsing fails - API will validate
        Long id = CliUtils.getLongParam(params, "id");
        if (id == null) {
            // id is needed for the URL path, so we need it to be valid
            System.err.println("Error: --id must be a valid number");
            System.exit(1);
        }

        Object carId = CliUtils.getLongOrStringParam(params, "carId");
        Object liters = CliUtils.getDoubleOrStringParam(params, "liters");
        Object price = CliUtils.getDoubleOrStringParam(params, "price");
        Object odometer = CliUtils.getIntegerOrStringParam(params, "odometer");

        Map<String, Object> fuelEntryData = new HashMap<>();
        fuelEntryData.put("carId", carId);
        fuelEntryData.put("liters", liters);
        fuelEntryData.put("price", price);
        fuelEntryData.put("odometer", odometer);
        String json = CliUtils.buildJson(fuelEntryData);

        HttpResponse<String> response = CliUtils.put(BASE_URL + "/" + id, json);
        CliUtils.printActionResponse(response);
    }

    /**
     * Deletes a fuel entry by ID.
     */
    public static void deleteFuelEntry(String[] args) throws IOException, InterruptedException {
        Map<String, String> params = CliUtils.parseArgs(args);
        Long id = CliUtils.getLongParam(params, "id");

        if (id == null) {
            System.err.println("Error: --id parameter is required");
            System.exit(1);
        }

        HttpResponse<String> response = CliUtils.delete(BASE_URL + "/" + id);
        CliUtils.printActionResponse(response);
    }
}

