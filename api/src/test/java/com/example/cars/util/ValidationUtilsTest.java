package com.example.cars.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ValidationUtilsTest {

    @InjectMocks
    private ValidationUtils validationUtils;

    private Predicate<Long> alwaysTrue;
    private Predicate<Long> alwaysFalse;

    @BeforeEach
    void setUp() {
        alwaysTrue = id -> true;
        alwaysFalse = id -> false;
    }

    @Test
    void validateIdNotNull_ValidId_DoesNotThrow() {
        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateIdNotNull(1L, "Car"));
    }

    @Test
    void validateIdNotNull_NullId_ThrowsBadRequest() {
        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> validationUtils.validateIdNotNull(null, "Car")
        );
        
        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Car ID cannot be null"));
    }

    @Test
    void validateEntityExists_ValidIdAndExists_DoesNotThrow() {
        // When & Then
        assertDoesNotThrow(() -> validationUtils.validateEntityExists(1L, alwaysTrue, "Car"));
    }

    @Test
    void validateEntityExists_NullId_ThrowsBadRequest() {
        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> validationUtils.validateEntityExists(null, alwaysTrue, "Car")
        );
        
        assertEquals(400, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Car ID cannot be null"));
    }

    @Test
    void validateEntityExists_EntityNotFound_ThrowsNotFound() {
        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> validationUtils.validateEntityExists(999L, alwaysFalse, "Car")
        );
        
        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Car with ID 999 not found"));
    }

    @Test
    void validateEntityExists_DifferentEntityName_UsesCorrectName() {
        // When & Then
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> validationUtils.validateEntityExists(999L, alwaysFalse, "Fuel entry")
        );
        
        assertEquals(404, exception.getStatusCode().value());
        assertTrue(exception.getReason().contains("Fuel entry with ID 999 not found"));
    }
}

