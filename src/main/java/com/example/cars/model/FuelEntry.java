package com.example.cars.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonPropertyOrder({"id", "carId", "liters", "price", "totalPrice", "odometer", "timestamp"})
public class FuelEntry {
    private Long id;
    private Long carId;
    private double liters;
    private double price;
    private double totalPrice;
    private int odometer;
    private LocalDateTime timestamp;

    // For JSON mapping
    public FuelEntry() {
    }

    // For creating a new fuel entry
    public FuelEntry(Long carId, double liters, double price, int odometer) {
        this.carId = carId;
        this.liters = liters;
        this.price = price;
        this.totalPrice = liters * price;
        this.odometer = odometer;
        this.timestamp = LocalDateTime.now();
    }

    // Full constructor
    public FuelEntry(Long id, Long carId, double liters, double price, double totalPrice, int odometer, LocalDateTime timestamp) {
        this.id = id;
        this.carId = carId;
        this.liters = liters;
        this.price = price;
        this.totalPrice = totalPrice;
        this.odometer = odometer;
        this.timestamp = timestamp;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getCarId() {
        return carId;
    }

    public double getLiters() {
        return liters;
    }

    public double getPrice() {
        return price;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }

    public int getOdometer() {
        return odometer;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }   

    public void setLiters(double liters) {
        this.liters = liters;
        this.totalPrice = liters * price;
    }

    public void setPrice(double price) {
        this.price = price;
        this.totalPrice = liters * price;
    }
    
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setOdometer(int odometer) {
        this.odometer = odometer;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
