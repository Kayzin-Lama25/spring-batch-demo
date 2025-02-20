package com.example.demo.dto;

public class TeamPerformance {

    private final String name;
    private final String performance;

    public TeamPerformance(String name, String performance) {
        this.name = name;
        this.performance = performance;
    }

    public String getName() {
        return name;
    }

    public String getPerformance() {
        return performance;
    }

}
