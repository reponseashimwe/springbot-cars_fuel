package com.example.cars.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.cars.model.Car;
import com.example.cars.service.CarService;

@RestController
@RequestMapping("api/cars")
public class CarController {
    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    // GET all cars
    @GetMapping
    public List<Car> getAllCars() {
        return carService.getAllCars();
    }

    // GET a car by id
    @GetMapping("/{carId}")
    public Car getCarById(@PathVariable Long carId) {
        return carService.getCarById(carId);
    }

    // POST a new car
    @PostMapping
    public ResponseEntity<Car> createCar(@RequestBody Car car) {
        Car createdCar = carService.createCar(car);
        return new ResponseEntity<>(createdCar, HttpStatus.CREATED);
    }
    
    // PUT an existing car
    @PutMapping("/{carId}")
    public Car updateCar(@PathVariable Long carId, @RequestBody Car car) {
        return carService.updateCar(carId, car);
    }
    
    // DELETE a car
    @DeleteMapping("/{carId}")
    public ResponseEntity<Map<String, String>> deleteCar(@PathVariable Long carId) {
        carService.deleteCar(carId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Car with ID " + carId + " has been successfully deleted");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
