package com.example.cars.util;

import java.util.function.Predicate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ValidationUtils {
    
    /**
     * Validates that an ID is not null.
     * 
     * @param id the ID to validate
     * @param entityName the name of the entity (e.g., "Car", "Fuel entry") for error messages
     * @throws ResponseStatusException if ID is null
     */
    public void validateIdNotNull(Long id, String entityName) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                entityName + " ID cannot be null");
        }
    }

    /**
     * Validates that an entity exists using the provided existence checker.
     * 
     * @param id the ID to check
     * @param existsChecker a predicate that checks if the entity exists
     * @param entityName the name of the entity for error messages
     * @throws ResponseStatusException if ID is null or entity not found
     */
    public void validateEntityExists(Long id, Predicate<Long> existsChecker, String entityName) {
        validateIdNotNull(id, entityName);
        if (!existsChecker.test(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                entityName + " with ID " + id + " not found");
        }
    }

    /**
     * Validates that an object is not null.
     * 
     * @param obj the object to validate
     * @param entityName the name of the entity for error messages
     * @throws ResponseStatusException if object is null
     */
    public void validateNotNull(Object obj, String entityName) {
        if (obj == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                entityName + " object cannot be null");
        }
    }

    /**
     * Validates that a string is not null or empty (after trimming).
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @throws ResponseStatusException if string is null or empty
     */
    public void validateStringNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " is required and cannot be empty");
        }
    }

    /**
     * Validates that a number is greater than zero.
     * 
     * @param value the number to validate
     * @param fieldName the name of the field for error messages
     * @throws ResponseStatusException if value is not greater than zero
     */
    public void validateGreaterThanZero(double value, String fieldName) {
        if (value <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " must be greater than 0");
        }
    }

    /**
     * Validates that a number is greater than or equal to zero.
     * 
     * @param value the number to validate
     * @param fieldName the name of the field for error messages
     * @throws ResponseStatusException if value is less than zero
     */
    public void validateGreaterThanOrEqualToZero(int value, String fieldName) {
        if (value < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " must be 0 or greater");
        }
    }

    /**
     * Validates that an Integer is not null and within the specified range.
     * 
     * @param value the number to validate
     * @param fieldName the name of the field for error messages
     * @param min the minimum allowed value (inclusive), or null if no minimum constraint
     * @param max the maximum allowed value (inclusive), or null if no maximum constraint
     * @throws ResponseStatusException if value is null or outside the specified range
     */
    public void validateNumber(Integer value, String fieldName, Integer min, Integer max) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " is required and cannot be null");
        }
        
        if (min != null && value < min) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " must be " + min + " or greater");
        }
        
        if (max != null && value > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " must be " + max + " or less");
        }
    }



    /**
     * Validates that an int is within the specified range.
     * 
     * @param value the number to validate
     * @param fieldName the name of the field for error messages
     * @param min the minimum allowed value (inclusive), or null if no minimum constraint
     * @param max the maximum allowed value (inclusive), or null if no maximum constraint
     * @throws ResponseStatusException if value is outside the specified range
     */
    public void validateNumber(int value, String fieldName, Integer min, Integer max) {
        if (min != null && value < min) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " must be " + min + " or greater");
        }
        
        if (max != null && value > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " must be " + max + " or less");
        }
    }

    /**
     * Validates that an int is greater than or equal to the minimum value.
     * 
     * @param value the number to validate
     * @param fieldName the name of the field for error messages
     * @param min the minimum allowed value (inclusive)
     * @throws ResponseStatusException if value is less than min
     */
    public void validateNumber(int value, String fieldName, Integer min) {
        validateNumber(value, fieldName, min, null);
    }

    /**
     * Validates that a double is within the specified range.
     * 
     * @param value the number to validate
     * @param fieldName the name of the field for error messages
     * @param min the minimum allowed value (inclusive), or null if no minimum constraint
     * @param max the maximum allowed value (inclusive), or null if no maximum constraint
     * @throws ResponseStatusException if value is outside the specified range
     */
    public void validateNumber(double value, String fieldName, Double min, Double max) {
        if (min != null && value < min) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " must be " + min + " or greater");
        }
        
        if (max != null && value > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " must be " + max + " or less");
        }
    }

    /**
     * Validates that a double is greater than or equal to the minimum value.
     * 
     * @param value the number to validate
     * @param fieldName the name of the field for error messages
     * @param min the minimum allowed value (inclusive)
     * @throws ResponseStatusException if value is less than min
     */
    public void validateNumber(double value, String fieldName, Double min) {
        validateNumber(value, fieldName, min, null);
    }

    /**
     * Validates that a string represents a valid integer.
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @return the parsed integer value
     * @throws ResponseStatusException if value is null, empty, or not a valid integer
     */
    public Integer validateAndParseInteger(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " is required and cannot be empty");
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " must be a valid integer. Received: '" + value + "'");
        }
    }

    /**
     * Validates that a string represents a valid integer within the specified range.
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @param min the minimum allowed value (inclusive), or null if no minimum constraint
     * @param max the maximum allowed value (inclusive), or null if no maximum constraint
     * @return the parsed integer value
     * @throws ResponseStatusException if value is null, empty, not a valid integer, or outside the range
     */
    public Integer validateAndParseInteger(String value, String fieldName, Integer min, Integer max) {
        Integer parsedValue = validateAndParseInteger(value, fieldName);
        validateNumber(parsedValue, fieldName, min, max);
        return parsedValue;
    }

    /**
     * Validates that a string represents a valid double.
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @return the parsed double value
     * @throws ResponseStatusException if value is null, empty, or not a valid double
     */
    public Double validateAndParseDouble(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " is required and cannot be empty");
        }
        
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " must be a valid number. Received: '" + value + "'");
        }
    }

    /**
     * Validates that a string represents a valid double within the specified range.
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @param min the minimum allowed value (inclusive), or null if no minimum constraint
     * @param max the maximum allowed value (inclusive), or null if no maximum constraint
     * @return the parsed double value
     * @throws ResponseStatusException if value is null, empty, not a valid double, or outside the range
     */
    public Double validateAndParseDouble(String value, String fieldName, Double min, Double max) {
        Double parsedValue = validateAndParseDouble(value, fieldName);
        validateNumber(parsedValue, fieldName, min, max);
        return parsedValue;
    }

    /**
     * Validates that a string represents a valid long.
     * 
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @return the parsed long value
     * @throws ResponseStatusException if value is null, empty, or not a valid long
     */
    public Long validateAndParseLong(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " is required and cannot be empty");
        }
        
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                fieldName + " must be a valid long integer. Received: '" + value + "'");
        }
    }
}

