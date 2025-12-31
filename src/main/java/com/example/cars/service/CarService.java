package com.example.cars.service;

import java.util.*;

import com.example.cars.model.Car;
import com.example.cars.util.ValidationUtils;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CarService {
    // In-memory storage for cars
    private final Map<Long, Car> cars = new HashMap<>();
    private final ValidationUtils validationUtils;

    private final AtomicLong idGenerator = new AtomicLong(1);

    public CarService(ValidationUtils validationUtils) {
        this.validationUtils = validationUtils;
    }

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
        validationUtils.validateNotNull(car, "Car");
        validationUtils.validateStringNotEmpty(car.getBrand(), "Brand");
        validationUtils.validateStringNotEmpty(car.getModel(), "Model");
        validateYear(car.getYear());
    }

    private void validateYear(Integer year) {
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        int minYear = 1886; // First car was made in 1886
        validationUtils.validateNumber(year, "Year", Integer.valueOf(minYear), Integer.valueOf(currentYear));
    }

    private void validateIdExists(Long id) {
        validationUtils.validateEntityExists(id, cars::containsKey, "Car");
    }
}
