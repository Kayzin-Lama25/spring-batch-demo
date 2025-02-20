package com.example.demo.dto;

import java.util.LinkedList;
import java.util.List;

public class Team {
    private final String name;
    private final List<ScoredPlayer> scoredPlayers = new LinkedList<>();

    public Team(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<ScoredPlayer> getScoredPlayers() {
        return scoredPlayers;
    }


    public static class ScoredPlayer {
        private final String name;
        private final List<Double> scores = new LinkedList<>();

        public ScoredPlayer(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public List<Double> getScores() {
            return scores;
        }

    }
}
