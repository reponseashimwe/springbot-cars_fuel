package com.example.cars.cli;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class CliUtils {
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses command-line arguments into a map of key-value pairs.
     * Expects arguments in the format: --key value
     */
    public static Map<String, String> parseArgs(String[] args) {
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--") && i + 1 < args.length) {
                String key = args[i].substring(2); // Remove "--" prefix
                String value = args[i + 1];
                params.put(key, value);
                i++; // Skip the value in next iteration
            }
        }
        return params;
    }

    /**
     * Gets a parameter value as String, or returns null if not found.
     */
    public static String getStringParam(Map<String, String> params, String key) {
        return params.get(key);
    }

    /**
     * Gets a parameter value as Long, or returns null if not found or invalid.
     */
    public static Long getLongParam(Map<String, String> params, String key) {
        String value = params.get(key);
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Gets a parameter value as Integer, or returns null if not found or invalid.
     */
    public static Integer getIntegerParam(Map<String, String> params, String key) {
        String value = params.get(key);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Performs a GET request to the specified URL.
     */
    public static HttpResponse<String> get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Performs a POST request to the specified URL with JSON body.
     */
    public static HttpResponse<String> post(String url, String jsonBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .header("Content-Type", "application/json")
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Performs a PUT request to the specified URL with JSON body.
     */
    public static HttpResponse<String> put(String url, String jsonBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
            .header("Content-Type", "application/json")
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Performs a DELETE request to the specified URL.
     */
    public static HttpResponse<String> delete(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .DELETE()
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Builds a JSON object from a map of key-value pairs.
     * Escapes string values properly.
     */
    public static String buildJson(Map<String, Object> data) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(escapeJson((String) value)).append("\"");
            } else {
                json.append(value);
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    /**
     * Escapes special characters in JSON strings.
     */
    private static String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * Prints the response body with pretty-printed JSON if applicable, and handles errors.
     */
    public static void printResponse(HttpResponse<String> response) {
        String body = response.body();
        
        // Try to pretty print if it's JSON
        String formattedBody = prettyPrintJson(body);
        System.out.println(formattedBody);
        
        if (response.statusCode() >= 400) {
            System.err.println("Error: HTTP " + response.statusCode());
        }
    }

    /**
     * Attempts to pretty print a JSON string. Returns the original string if it's not valid JSON.
     */
    private static String prettyPrintJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return jsonString;
        }
        
        try {
            // Parse the JSON
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            // Pretty print with 2-space indentation
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (Exception e) {
            // If it's not valid JSON, return as-is
            return jsonString;
        }
    }
}

