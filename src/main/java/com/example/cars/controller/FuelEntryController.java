package com.example.cars.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.cars.model.FuelEntry;
import com.example.cars.service.FuelEntryService;

@RestController
@RequestMapping("/api/fuel-entries")
public class FuelEntryController {
    private final FuelEntryService fuelEntryService;

    public FuelEntryController(FuelEntryService fuelEntryService) {
        this.fuelEntryService = fuelEntryService;
    }

    // GET all fuel entries
    @GetMapping
    public List<FuelEntry> getAllFuelEntries() {
        return fuelEntryService.getAllFuelEntries();
    }

    // GET a fuel entry by id
    @GetMapping("/{id}")
    public FuelEntry getFuelEntryById(@PathVariable Long id) {
        return fuelEntryService.getFuelEntryById(id);
    }

    // POST a new fuel entry
    @PostMapping
    public ResponseEntity<FuelEntry> createFuelEntry(@RequestBody FuelEntry fuelEntry) {
        FuelEntry createdFuelEntry = fuelEntryService.createFuelEntry(fuelEntry);
        return new ResponseEntity<>(createdFuelEntry, HttpStatus.CREATED);
    }

    // PUT an existing fuel entry
    @PutMapping("/{id}")
    public FuelEntry updateFuelEntry(@PathVariable Long id, @RequestBody FuelEntry fuelEntry) {
        return fuelEntryService.updateFuelEntry(id, fuelEntry);
    }

    // DELETE a fuel entry
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteFuelEntry(@PathVariable Long id) {
        fuelEntryService.deleteFuelEntry(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Fuel entry with ID " + id + " has been successfully deleted");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
