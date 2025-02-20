package com.example.demo.dto;

public class AverageScoredTeam {
    
    private final String name;
    private final double averageScored;

    public AverageScoredTeam(String name, double averageScored) {
        this.name = name;
        this.averageScored = averageScored;
    }

    public String getName() {
        return name;
    }

    public double getAverageScored() {
        return averageScored;
    }
}
