package com.example.demo.steps.reader;

import java.util.Optional;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;

import com.example.demo.dto.Team;
import com.example.demo.dto.Team.ScoredPlayer;

public class DivisionFileReader implements ResourceAwareItemReaderItemStream<Team> {

    private final FlatFileItemReader<String> delegateReader;
    
    public DivisionFileReader(FlatFileItemReader<String> delegateReader) {
        this.delegateReader = delegateReader;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        delegateReader.open(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        delegateReader.close();
    }

    @Override
    @Nullable
    public Team read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        Optional<Team> maybeTeam = Optional.empty();
        String line;

        while ((line = delegateReader.read()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                return maybeTeam.orElse(null);
            } else if (!line.contains(":")) {
                maybeTeam = Optional.of(new Team(line));
            } else {
                final String[] nameAndScores = line.split(":");
                maybeTeam.ifPresent(team -> team.getScoredPlayers().add(parseScoredPlayer(nameAndScores)));
            }
        }

        return maybeTeam.orElse(null);
    }

    @Override
    public void setResource(Resource resource) {
        delegateReader.setResource(resource);
    }

    private ScoredPlayer parseScoredPlayer(String[] nameAndScores) {
        String name = nameAndScores[0];
        String[] scores = nameAndScores[1].split(",");

        ScoredPlayer scoredPlayer = new ScoredPlayer(name);
        for (String score: scores) {
            scoredPlayer.getScores().add(Double.parseDouble(score));
        }

        return scoredPlayer;
    }
    
}
