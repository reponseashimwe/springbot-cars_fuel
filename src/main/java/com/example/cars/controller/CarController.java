package com.example.cars.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.cars.model.Car;
import com.example.cars.service.CarService;
import com.example.cars.service.FuelEntryService;
import com.example.cars.model.FuelEntry;

@RestController
@RequestMapping("/api/cars")
public class CarController {
    private final CarService carService;
    private final FuelEntryService fuelEntryService;

    public CarController(CarService carService, FuelEntryService fuelEntryService) {
        this.carService = carService;
        this.fuelEntryService = fuelEntryService;
    }

    // GET all cars
    @GetMapping
    public List<Car> getAllCars() {
        return carService.getAllCars();
    }

    // GET a car by id
    @GetMapping("/{id}")
    public Car getCarById(@PathVariable Long id) {
        return carService.getCarById(id);
    }

    // POST a new car
    @PostMapping
    public ResponseEntity<Car> createCar(@RequestBody Car car) {
        Car createdCar = carService.createCar(car);
        return new ResponseEntity<>(createdCar, HttpStatus.CREATED);
    }
    
    // PUT an existing car
    @PutMapping("/{id}")
    public Car updateCar(@PathVariable Long id, @RequestBody Car car) {
        return carService.updateCar(id, car);
    }
    
    // DELETE a car
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Car with ID " + id + " has been successfully deleted");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Fill a car with fuel
    @PostMapping("/{id}/fuel")
    public ResponseEntity<FuelEntry> fillCarWithFuel(@PathVariable Long id, @RequestBody FuelEntry fuelEntry) {
        fuelEntry.setCarId(id);
        FuelEntry createdFuelEntry = fuelEntryService.createFuelEntry(fuelEntry);
        return new ResponseEntity<>(createdFuelEntry, HttpStatus.CREATED);
    }

    // GET all fuel entries for a car
    @GetMapping("/{id}/fuel")
    public List<FuelEntry> getAllFuelEntries(@PathVariable Long id) {
        return fuelEntryService.getAllFuelEntriesByCarId(id);
    }

    // GET fuel stats for a car
    @GetMapping("/{id}/fuel/stats")
    public Map<String, Double> getFuelStats(@PathVariable Long id) {
        return fuelEntryService.getFuelStats(id);
    }
}
