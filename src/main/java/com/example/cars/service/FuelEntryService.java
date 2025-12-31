package com.example.cars.service;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import com.example.cars.model.FuelEntry;
import com.example.cars.util.ValidationUtils;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class FuelEntryService {
    // In-memory storage for fuel entries
    private final Map<Long, FuelEntry> fuelEntries = new HashMap<>();
    private final CarService carService;
    private final ValidationUtils validationUtils;

    private final AtomicLong idGenerator = new AtomicLong(1);

    public FuelEntryService(CarService carService, ValidationUtils validationUtils) {
        this.carService = carService;
        this.validationUtils = validationUtils;
    }

    // Create a new fuel entry
    public FuelEntry createFuelEntry(FuelEntry fuelEntry) {
        validateFuelEntry(fuelEntry);
        // Validate that the car exists
        validateCarIdExists(fuelEntry.getCarId());
        long id = idGenerator.getAndIncrement();
        fuelEntry.setId(id);
        // Set timestamp if not already set
        if (fuelEntry.getTimestamp() == null) {
            fuelEntry.setTimestamp(LocalDateTime.now());
        }
        fuelEntries.put(id, fuelEntry);

        return fuelEntry;
    }

    // Get all fuel entries
    public List<FuelEntry> getAllFuelEntries() {
        return new ArrayList<>(fuelEntries.values());
    }

    // Get a fuel entry by id
    public FuelEntry getFuelEntryById(Long id) {
        validateIdExists(id);
        FuelEntry fuelEntry = fuelEntries.get(id);
        return fuelEntry;
    }
    
    // Update a fuel entry
    public FuelEntry updateFuelEntry(Long id, FuelEntry fuelEntry) {
        validateIdExists(id);
        validateFuelEntry(fuelEntry);
        FuelEntry existingFuelEntry = getFuelEntryById(id);
        existingFuelEntry.setLiters(fuelEntry.getLiters());
        existingFuelEntry.setPrice(fuelEntry.getPrice());
        existingFuelEntry.setOdometer(fuelEntry.getOdometer());

        fuelEntries.put(id, existingFuelEntry);

        return existingFuelEntry;
    }

    // Delete a fuel entry
    public void deleteFuelEntry(Long id) {
        validateIdExists(id);

        fuelEntries.remove(id);
    }

    // Fuel Stats
    public Map<String, Double> getFuelStats(Long carId) {
        validateCarIdExists(carId);
        List<FuelEntry> fuelEntries = getAllFuelEntriesByCarId(carId);
        if (fuelEntries.isEmpty()) {
            // Return zeros if no fuel entries exist
            Map<String, Double> emptyStats = new HashMap<>();
            emptyStats.put("totalLiters", 0.0);
            emptyStats.put("totalPrice", 0.0);
            emptyStats.put("avgPer100km", 0.0);
            return emptyStats;
        }
        return calculateFuelStats(fuelEntries);
    }   

    // Calculate fuel stats
    // Formula: Average (L/100km) = (Total fuel used / Total distance driven) × 100
    // Distance = last odometer reading - first odometer reading
    private Map<String, Double> calculateFuelStats(List<FuelEntry> fuelEntries) {
        Map<String, Double> stats = new HashMap<>();
        
        // Sum all fuel used across all entries
        double totalLiters = fuelEntries.stream().mapToDouble(FuelEntry::getLiters).sum();
        double totalPrice = fuelEntries.stream().mapToDouble(FuelEntry::getTotalPrice).sum();
        
        stats.put("totalLiters", totalLiters);
        stats.put("totalPrice", totalPrice);
        
        // Calculate total distance driven: max odometer - min odometer
        // Sort entries by odometer to find min and max values
        List<FuelEntry> sortedEntries = fuelEntries.stream()
            .sorted(Comparator.comparingInt(FuelEntry::getOdometer))
            .collect(Collectors.toList());
        
        int minOdometer = sortedEntries.get(0).getOdometer();
        int maxOdometer = sortedEntries.get(sortedEntries.size() - 1).getOdometer();
        
        // If there's only one entry or entries have same odometer, assume previous was 0
        // Otherwise, calculate distance from min to max odometer
        int totalDistance;
        if (sortedEntries.size() == 1 || minOdometer == maxOdometer) {
            // No previous odometer available, assume car started at 0
            totalDistance = maxOdometer;
        } else {
            totalDistance = maxOdometer - minOdometer;
        }
        
        // Calculate average fuel consumption per 100km
        // Formula: (Total fuel used / Total distance driven) × 100
        double avgPer100km = 0.0;
        if (totalDistance > 0) {
            avgPer100km = (totalLiters / totalDistance) * 100.0;
            // Round to 2 decimal places
            avgPer100km = Math.round(avgPer100km * 100.0) / 100.0;
        }
        
        stats.put("avgPer100km", avgPer100km);
        
        return stats;
    }

    // Get all fuel entries by car id
    public List<FuelEntry> getAllFuelEntriesByCarId(Long carId) {
        validateCarIdExists(carId);
        return fuelEntries.values().stream().filter(fuelEntry -> fuelEntry.getCarId().equals(carId)).collect(Collectors.toList());
    }

    // Validate fuel entry
    private void validateFuelEntry(FuelEntry fuelEntry) {
        validationUtils.validateNotNull(fuelEntry, "Fuel entry");
        validationUtils.validateNumber(fuelEntry.getLiters(), "Liters", 0.0);
        validationUtils.validateNumber(fuelEntry.getPrice(), "Price", 0.0);
        validationUtils.validateNumber(fuelEntry.getOdometer(), "Odometer", 0);
    }

    private void validateIdExists(Long id) {
        validationUtils.validateEntityExists(id, fuelEntries::containsKey, "Fuel entry");
    }

    private void validateCarIdExists(Long carId) {
        validationUtils.validateIdNotNull(carId, "Car");
        // Validate car exists by trying to get it
        carService.getCarById(carId);
    }
}
