package com.example.cars.service;

import java.util.*;

import com.example.cars.model.Car;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CarService {
    // In-memory storage for cars
    private final Map<Long, Car> cars = new HashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(1);

    // Create a new car
    public Car createCar(Car car) {
        validateCar(car);
        long id = idGenerator.getAndIncrement();
        car.setId(id);
        cars.put(id, car);

        return car;
    }

    // Get all cars
    public List<Car> getAllCars() {
        return new ArrayList<>(cars.values());
    }

    // Get a car by id
    public Car getCarById(Long id) {
        validateIdExists(id);
        Car car = cars.get(id);
        return car;
    }
    // Update a car
    public Car updateCar(Long id, Car car) {
        validateIdExists(id);
        validateCar(car);
        Car existingCar = getCarById(id);
        existingCar.setBrand(car.getBrand());
        existingCar.setModel(car.getModel());
        existingCar.setYear(car.getYear());
        cars.put(id, existingCar);

        return existingCar;
    }

    // Delete a car
    public void deleteCar(Long id) {
        validateIdExists(id);

        cars.remove(id);
    }
 

    private void validateCar(Car car) {
        validateCarNotNull(car);
        validateBrand(car.getBrand());
        validateModel(car.getModel());
        validateYear(car.getYear());
    }

    private void validateCarNotNull(Car car) {
        if (car == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Car object cannot be null");
        }
    }

    private void validateBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand is required and cannot be empty");
        }
    }

    private void validateModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Model is required and cannot be empty");
        }
    }

    private void validateYear(Integer year) {
        if (year == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Year is required and cannot be null");
        }
        
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        int minYear = 1886; // First car was made in 1886
        
        if (year < minYear) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Year must be " + minYear + " or later (first car was made in " + minYear + ")");
        }
        if (year > currentYear + 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Year cannot be more than " + (currentYear + 1) + " (current year + 1)");
        }
    }

    private void validateIdExists(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Car ID cannot be null");
        }
        if (!cars.containsKey(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Car with ID " + id + " not found");
        }
    }
}
