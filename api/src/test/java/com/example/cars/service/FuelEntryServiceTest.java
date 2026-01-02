package com.example.cars.service;

import com.example.cars.model.Car;
import com.example.cars.model.FuelEntry;
import com.example.cars.repository.FuelEntryRepository;
import com.example.cars.util.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuelEntryServiceTest {

    @Mock
    private FuelEntryRepository fuelEntryRepository;

    @Mock
    private CarService carService;

    private ValidationUtils validationUtils = new ValidationUtils();

    @InjectMocks
    private FuelEntryService fuelEntryService;
    
    @org.junit.jupiter.api.BeforeEach
    void setUpMocks() {
        // Inject the real ValidationUtils since we need it to actually call existsById
        fuelEntryService = new FuelEntryService(fuelEntryRepository, carService, validationUtils);
    }

    private FuelEntry testFuelEntry;
    private Car testCar;

    @BeforeEach
    void setUp() {
        testCar = new Car(1L, "Toyota", "Corolla", 2020);
        testFuelEntry = new FuelEntry(1L, 1L, 40.0, 1.30, 52.0, 10000, LocalDateTime.now());
    }

    @Test
    void createFuelEntry_ValidEntry_ReturnsCreatedEntry() {
        // Given
        FuelEntry newEntry = new FuelEntry(1L, 40.0, 1.30, 10000);
        when(carService.getCarById(1L)).thenReturn(testCar);
        when(fuelEntryRepository.findByCarId(1L)).thenReturn(List.of());
        when(fuelEntryRepository.save(any(FuelEntry.class))).thenReturn(testFuelEntry);

        // When
        FuelEntry result = fuelEntryService.createFuelEntry(newEntry);

        // Then
        assertNotNull(result);
        assertNotNull(result.getTimestamp());
        // getCarById is called twice: once in validateCarIdExists, once in getAllFuelEntriesByCarId
        verify(carService, times(2)).getCarById(1L);
        verify(fuelEntryRepository).save(any(FuelEntry.class));
    }

    @Test
    void createFuelEntry_DecreasingOdometer_ThrowsException() {
        // Given
        FuelEntry newEntry = new FuelEntry(1L, 40.0, 1.30, 5000);
        when(carService.getCarById(1L)).thenReturn(testCar);
        when(fuelEntryRepository.findByCarId(1L)).thenReturn(
            Arrays.asList(new FuelEntry(1L, 1L, 30.0, 1.25, 37.5, 10000, LocalDateTime.now()))
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> fuelEntryService.createFuelEntry(newEntry));
        verify(fuelEntryRepository, never()).save(any(FuelEntry.class));
    }

    @Test
    void getFuelStats_ValidCarId_ReturnsStats() {
        // Given
        when(carService.getCarById(1L)).thenReturn(testCar);
        List<FuelEntry> entries = Arrays.asList(
            new FuelEntry(1L, 1L, 40.0, 1.30, 52.0, 10000, LocalDateTime.now().minusDays(2)),
            new FuelEntry(2L, 1L, 35.0, 1.35, 47.25, 15000, LocalDateTime.now().minusDays(1)),
            new FuelEntry(3L, 1L, 30.0, 1.40, 42.0, 20000, LocalDateTime.now())
        );
        when(fuelEntryRepository.findByCarId(1L)).thenReturn(entries);

        // When
        Map<String, Double> stats = fuelEntryService.getFuelStats(1L);

        // Then
        assertNotNull(stats);
        assertEquals(105.0, stats.get("totalLiters")); // 40 + 35 + 30
        assertEquals(141.25, stats.get("totalPrice")); // 52 + 47.25 + 42
        assertTrue(stats.get("avgPer100km") > 0);
        // getCarById is called twice: once in validateCarIdExists, once in getAllFuelEntriesByCarId
        verify(carService, times(2)).getCarById(1L);
        verify(fuelEntryRepository).findByCarId(1L);
    }

    @Test
    void getFuelStats_NoEntries_ReturnsZeroStats() {
        // Given
        when(carService.getCarById(1L)).thenReturn(testCar);
        when(fuelEntryRepository.findByCarId(1L)).thenReturn(List.of());

        // When
        Map<String, Double> stats = fuelEntryService.getFuelStats(1L);

        // Then
        assertNotNull(stats);
        assertEquals(0.0, stats.get("totalLiters"));
        assertEquals(0.0, stats.get("totalPrice"));
        assertEquals(0.0, stats.get("avgPer100km"));
    }

    @Test
    void getFuelStats_SingleEntry_ReturnsZeroAvgConsumption() {
        // Given
        when(carService.getCarById(1L)).thenReturn(testCar);
        List<FuelEntry> singleEntry = Arrays.asList(
            new FuelEntry(1L, 1L, 40.0, 1.30, 52.0, 10000, LocalDateTime.now())
        );
        when(fuelEntryRepository.findByCarId(1L)).thenReturn(singleEntry);

        // When
        Map<String, Double> stats = fuelEntryService.getFuelStats(1L);

        // Then
        assertNotNull(stats);
        assertEquals(40.0, stats.get("totalLiters"));
        assertEquals(52.0, stats.get("totalPrice"));
        assertEquals(0.0, stats.get("avgPer100km")); // No distance traveled with single entry
        // getCarById is called twice: once in validateCarIdExists, once in getAllFuelEntriesByCarId
        verify(carService, times(2)).getCarById(1L);
        verify(fuelEntryRepository).findByCarId(1L);
    }

    @Test
    void getFuelStats_InvalidCarId_ThrowsException() {
        // Given
        when(carService.getCarById(999L)).thenThrow(new IllegalArgumentException("Car not found with id: 999"));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> fuelEntryService.getFuelStats(999L));
        verify(fuelEntryRepository, never()).findByCarId(anyLong());
    }

    @Test
    void getAllFuelEntriesByCarId_ValidCarId_ReturnsEntries() {
        // Given
        when(carService.getCarById(1L)).thenReturn(testCar);
        List<FuelEntry> entries = Arrays.asList(testFuelEntry);
        when(fuelEntryRepository.findByCarId(1L)).thenReturn(entries);

        // When
        List<FuelEntry> result = fuelEntryService.getAllFuelEntriesByCarId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(carService).getCarById(1L);
        verify(fuelEntryRepository).findByCarId(1L);
    }

    @Test
    void deleteFuelEntry_ValidId_DeletesEntry() {
        // Given
        when(fuelEntryRepository.existsById(1L)).thenReturn(true);

        // When
        fuelEntryService.deleteFuelEntry(1L);

        // Then
        // validateEntityExists calls existsById internally
        verify(fuelEntryRepository).existsById(1L);
        verify(fuelEntryRepository).delete(1L);
    }
}

