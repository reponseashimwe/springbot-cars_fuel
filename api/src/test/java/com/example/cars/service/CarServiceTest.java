package com.example.cars.service;

import com.example.cars.model.Car;
import com.example.cars.repository.CarRepository;
import com.example.cars.util.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    private ValidationUtils validationUtils = new ValidationUtils();

    @InjectMocks
    private CarService carService;
    
    @org.junit.jupiter.api.BeforeEach
    void setUpMocks() {
        // Inject the real ValidationUtils since we need it to actually call existsById
        carService = new CarService(carRepository, validationUtils);
    }

    private Car testCar;

    @BeforeEach
    void setUp() {
        testCar = new Car(1L, "Toyota", "Corolla", 2020);
    }

    @Test
    void createCar_ValidCar_ReturnsCreatedCar() {
        // Given
        Car newCar = new Car("Toyota", "Corolla", 2020);
        when(carRepository.save(any(Car.class))).thenReturn(testCar);

        // When
        Car result = carService.createCar(newCar);

        // Then
        assertNotNull(result);
        assertEquals(testCar.getId(), result.getId());
        assertEquals(testCar.getBrand(), result.getBrand());
        verify(carRepository).save(newCar);
    }

    @Test
    void createCar_InvalidYear_ThrowsException() {
        // Given
        Car invalidCar = new Car("Toyota", "Corolla", 2030); // Future year

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> carService.createCar(invalidCar));
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    void getAllCars_ReturnsListOfCars() {
        // Given
        List<Car> cars = Arrays.asList(testCar, new Car(2L, "Honda", "Civic", 2019));
        when(carRepository.findAll()).thenReturn(cars);

        // When
        List<Car> result = carService.getAllCars();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(carRepository).findAll();
    }

    @Test
    void getCarById_ValidId_ReturnsCar() {
        // Given
        when(carRepository.existsById(1L)).thenReturn(true);
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));

        // When
        Car result = carService.getCarById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testCar.getId(), result.getId());
        // validateEntityExists calls existsById internally
        verify(carRepository).existsById(1L);
        verify(carRepository).findById(1L);
    }

    @Test
    void getCarById_InvalidId_ThrowsException() {
        // Given
        when(carRepository.existsById(999L)).thenReturn(false);

        // When & Then
        // validateEntityExists throws ResponseStatusException when entity doesn't exist
        assertThrows(ResponseStatusException.class, () -> carService.getCarById(999L));
        verify(carRepository).existsById(999L);
        verify(carRepository, never()).findById(anyLong());
    }

    @Test
    void updateCar_ValidCar_ReturnsUpdatedCar() {
        // Given
        Car updatedCar = new Car("Toyota", "Camry", 2021);
        when(carRepository.existsById(1L)).thenReturn(true);
        when(carRepository.findById(1L)).thenReturn(Optional.of(testCar));
        when(carRepository.save(any(Car.class))).thenReturn(testCar);

        // When
        Car result = carService.updateCar(1L, updatedCar);

        // Then
        assertNotNull(result);
        // validateEntityExists is called twice (once in updateCar, once in getCarById)
        // Each call internally uses existsById
        verify(carRepository, times(2)).existsById(1L);
        verify(carRepository).findById(1L);
        verify(carRepository).save(any(Car.class));
    }

    @Test
    void deleteCar_ValidId_DeletesCar() {
        // Given
        when(carRepository.existsById(1L)).thenReturn(true);

        // When
        carService.deleteCar(1L);

        // Then
        // validateEntityExists calls existsById internally
        verify(carRepository).existsById(1L);
        verify(carRepository).delete(1L);
    }

    @Test
    void deleteCar_InvalidId_ThrowsException() {
        // Given
        when(carRepository.existsById(999L)).thenReturn(false);

        // When & Then
        // validateEntityExists throws ResponseStatusException when entity doesn't exist
        assertThrows(ResponseStatusException.class, () -> carService.deleteCar(999L));
        verify(carRepository).existsById(999L);
        verify(carRepository, never()).delete(anyLong());
    }
}

