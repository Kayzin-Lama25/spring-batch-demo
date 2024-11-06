package com.example.demo.dto;

import java.util.List;

import lombok.Data;

@Data
public class DailySensorData {
    
    // Represented as 'MM-dd-yyyy'
    private final String date;

    private final List<Double> measurements;



}
