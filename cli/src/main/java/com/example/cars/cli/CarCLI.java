package com.example.cars.cli;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class CarCLI {
    private static final String BASE_URL = "http://localhost:8080/api/cars";

    /**
     * Lists all cars.
     */
    public static void listCars() throws IOException, InterruptedException {
        HttpResponse<String> response = CliUtils.get(BASE_URL);
        CliUtils.printListResponse(response);
    }

    /**
     * Gets a car by ID.
     */
    public static void getCar(String[] args) throws IOException, InterruptedException {
        Map<String, String> params = CliUtils.parseArgs(args);
        Long carId = CliUtils.getLongParam(params, "carId");

        if (carId == null) {
            System.err.println("Error: --carId parameter is required");
            System.exit(1);
        }

        HttpResponse<String> response = CliUtils.get(BASE_URL + "/" + carId);
        CliUtils.printListResponse(response);
    }

    /**
     * Creates a new car.
     */
    public static void createCar(String[] args) throws IOException, InterruptedException {
        Map<String, String> params = CliUtils.parseArgs(args);
        String brand = CliUtils.getStringParam(params, "brand");
        String model = CliUtils.getStringParam(params, "model");
        String yearStr = CliUtils.getStringParam(params, "year");

        if (brand == null || model == null || yearStr == null) {
            System.err.println("Error: --brand, --model, and --year parameters are required");
            System.exit(1);
        }

        // Try to parse year, but use raw string if parsing fails - API will validate
        Object year = CliUtils.getIntegerOrStringParam(params, "year");

        Map<String, Object> carData = new HashMap<>();
        carData.put("brand", brand);
        carData.put("model", model);
        carData.put("year", year);
        String json = CliUtils.buildJson(carData);

        HttpResponse<String> response = CliUtils.post(BASE_URL, json);
        CliUtils.printActionResponse(response);
    }

    /**
     * Updates an existing car.
     */
    public static void updateCar(String[] args) throws IOException, InterruptedException {
        Map<String, String> params = CliUtils.parseArgs(args);
        Long carId = CliUtils.getLongParam(params, "carId");
        String brand = CliUtils.getStringParam(params, "brand");
        String model = CliUtils.getStringParam(params, "model");
        String yearStr = CliUtils.getStringParam(params, "year");

        if (carId == null) {
            System.err.println("Error: --carId parameter is required");
            System.exit(1);
        }
        if (brand == null || model == null || yearStr == null) {
            System.err.println("Error: --brand, --model, and --year parameters are required");
            System.exit(1);
        }

        // Try to parse year, but use raw string if parsing fails - API will validate
        Object year = CliUtils.getIntegerOrStringParam(params, "year");

        Map<String, Object> carData = new HashMap<>();
        carData.put("brand", brand);
        carData.put("model", model);
        carData.put("year", year);
        String json = CliUtils.buildJson(carData);

        HttpResponse<String> response = CliUtils.put(BASE_URL + "/" + carId, json);
        CliUtils.printActionResponse(response);
    }

    /**
     * Deletes a car by ID.
     */
    public static void deleteCar(String[] args) throws IOException, InterruptedException {
        Map<String, String> params = CliUtils.parseArgs(args);
        Long carId = CliUtils.getLongParam(params, "carId");

        if (carId == null) {
            System.err.println("Error: --carId parameter is required");
            System.exit(1);
        }

        HttpResponse<String> response = CliUtils.delete(BASE_URL + "/" + carId);
        CliUtils.printActionResponse(response);
    }
}

