package com.example.demo.dto;

import com.example.demo.constants.AnomalyType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataAnomaly {
    
    private String date;
    private AnomalyType type;
    private double value;

    public DataAnomaly(String date, AnomalyType type, double value) {
        this.date = date;
        this.type = type;
        this.value = value;
    }
}
