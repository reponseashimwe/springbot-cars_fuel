package com.example.cars.service;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import com.example.cars.model.FuelEntry;
import com.example.cars.repository.FuelEntryRepository;
import com.example.cars.util.ValidationUtils;
import org.springframework.stereotype.Service;

@Service
public class FuelEntryService {
    private final FuelEntryRepository fuelEntryRepository;
    private final CarService carService;
    private final ValidationUtils validationUtils;

    public FuelEntryService(FuelEntryRepository fuelEntryRepository, CarService carService, ValidationUtils validationUtils) {
        this.fuelEntryRepository = fuelEntryRepository;
        this.carService = carService;
        this.validationUtils = validationUtils;
    }

    // Create a new fuel entry
    public FuelEntry createFuelEntry(FuelEntry fuelEntry) {
        // Validate that the car exists
        validateCarIdExists(fuelEntry.getCarId());
        // Validate odometer doesn't decrease
        validateOdometerNotDecreasing(fuelEntry.getCarId(), fuelEntry.getOdometer());
        // Set timestamp if not already set
        if (fuelEntry.getTimestamp() == null) {
            fuelEntry.setTimestamp(LocalDateTime.now());
        }
        return fuelEntryRepository.save(fuelEntry);
    }

    // Get all fuel entries
    public List<FuelEntry> getAllFuelEntries() {
        return fuelEntryRepository.findAll();
    }

    // Get a fuel entry by id
    public FuelEntry getFuelEntryById(Long id) {
        validateIdExists(id);
        return fuelEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fuel entry not found with id: " + id));
    }
    
    // Update a fuel entry
    public FuelEntry updateFuelEntry(Long id, FuelEntry fuelEntry) {
        validateIdExists(id);
        FuelEntry existingFuelEntry = getFuelEntryById(id);
        // Validate odometer against previous and next entries (by timestamp)
        validateOdometerForUpdate(existingFuelEntry.getCarId(), existingFuelEntry.getId(), existingFuelEntry.getTimestamp(), fuelEntry.getOdometer());
        existingFuelEntry.setLiters(fuelEntry.getLiters());
        existingFuelEntry.setPrice(fuelEntry.getPrice());
        existingFuelEntry.setOdometer(fuelEntry.getOdometer());

        return fuelEntryRepository.save(existingFuelEntry);
    }

    // Delete a fuel entry
    public void deleteFuelEntry(Long id) {
        validateIdExists(id);
        fuelEntryRepository.delete(id);
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
    // Formula: Average (L/100km) = (Total fuel consumed / Total distance driven) × 100
    // Note: Most recent entry (by timestamp) is excluded from consumption calculation as it represents fuel still in tank
    private Map<String, Double> calculateFuelStats(List<FuelEntry> fuelEntries) {
        Map<String, Double> stats = new HashMap<>();
        
        // Sum all fuel added across all entries (for totalLiters and totalPrice stats)
        double totalLiters = fuelEntries.stream().mapToDouble(FuelEntry::getLiters).sum();
        double totalPrice = fuelEntries.stream().mapToDouble(FuelEntry::getTotalPrice).sum();
        
        stats.put("totalLiters", totalLiters);
        stats.put("totalPrice", totalPrice);
        stats.put("avgPer100km", calculateAverageConsumption(fuelEntries));
        
        return stats;
    }
    
    // Calculates average fuel consumption per 100km (optimized single-pass algorithm)
    private double calculateAverageConsumption(List<FuelEntry> fuelEntries) {
        // Need at least 2 entries to calculate distance traveled
        if (fuelEntries.size() < 2) {
            return 0.0;
        }
        
        // Single pass: find min/max odometer, most recent entry, and total fuel
        int minOdometer = Integer.MAX_VALUE;
        int maxOdometer = Integer.MIN_VALUE;
        FuelEntry mostRecentEntry = null;
        LocalDateTime mostRecentTimestamp = null;
        double totalFuel = 0.0;
        
        for (FuelEntry entry : fuelEntries) {
            int odometer = entry.getOdometer();
            if (odometer < minOdometer) {
                minOdometer = odometer;
            }
            if (odometer > maxOdometer) {
                maxOdometer = odometer;
            }
            
            LocalDateTime timestamp = entry.getTimestamp();
            if (mostRecentTimestamp == null || (timestamp != null && timestamp.isAfter(mostRecentTimestamp))) {
                mostRecentTimestamp = timestamp;
                mostRecentEntry = entry;
            }
            
            totalFuel += entry.getLiters();
        }
        
        // Calculate distance
        int totalDistance = maxOdometer - minOdometer;
        if (totalDistance <= 0) {
            return 0.0;
        }
        
        // Calculate fuel consumed (exclude most recent entry - fuel still in tank)
        double fuelConsumed = totalFuel;
        if (mostRecentEntry != null) {
            fuelConsumed -= mostRecentEntry.getLiters();
        }
        
        // Formula: (Total fuel consumed / Total distance driven) × 100
        double avgPer100km = (fuelConsumed / totalDistance) * 100.0;
        return Math.round(avgPer100km * 100.0) / 100.0;
    }

    // Get all fuel entries by car id
    public List<FuelEntry> getAllFuelEntriesByCarId(Long carId) {
        validateCarIdExists(carId);
        return fuelEntryRepository.findByCarId(carId);
    }


    private void validateIdExists(Long id) {
        validationUtils.validateEntityExists(id, fuelEntryRepository::existsById, "Fuel entry");
    }

    private void validateCarIdExists(Long carId) {
        validationUtils.validateIdNotNull(carId, "Car");
        // Validate car exists by trying to get it
        carService.getCarById(carId);
    }

    private void validateOdometerNotDecreasing(Long carId, int newOdometer) {
        List<FuelEntry> existingEntries = getAllFuelEntriesByCarId(carId);
        if (!existingEntries.isEmpty()) {
            int maxOdometer = existingEntries.stream()
                    .mapToInt(FuelEntry::getOdometer)
                    .max()
                    .orElse(0);
            if (newOdometer < maxOdometer) {
                throw new IllegalArgumentException("Odometer cannot decrease. Maximum odometer for this car: " + maxOdometer + ", New: " + newOdometer);
            }
        }
    }

    private void validateOdometerForUpdate(Long carId, Long entryId, LocalDateTime entryTimestamp, int newOdometer) {
        List<FuelEntry> allEntries = getAllFuelEntriesByCarId(carId);
        // Sort by timestamp
        List<FuelEntry> sortedEntries = allEntries.stream()
                .sorted(Comparator.comparing(FuelEntry::getTimestamp))
                .collect(Collectors.toList());
        
        // Find the entry being updated
        int currentIndex = -1;
        for (int i = 0; i < sortedEntries.size(); i++) {
            if (sortedEntries.get(i).getId().equals(entryId)) {
                currentIndex = i;
                break;
            }
        }
        
        if (currentIndex == -1) {
            return; // Entry not found in list, skip validation
        }
        
        // Check previous entry (before this one by timestamp)
        if (currentIndex > 0) {
            FuelEntry previousEntry = sortedEntries.get(currentIndex - 1);
            if (newOdometer < previousEntry.getOdometer()) {
                throw new IllegalArgumentException("Odometer cannot be below previous entry. Previous odometer: " + previousEntry.getOdometer() + ", New: " + newOdometer);
            }
        }
        
        // Check next entry (after this one by timestamp)
        if (currentIndex < sortedEntries.size() - 1) {
            FuelEntry nextEntry = sortedEntries.get(currentIndex + 1);
            if (newOdometer > nextEntry.getOdometer()) {
                throw new IllegalArgumentException("Odometer cannot be above next entry. Next odometer: " + nextEntry.getOdometer() + ", New: " + newOdometer);
            }
        }
    }
}
