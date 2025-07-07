package com.example.demo.steps.listeners;

import java.util.Random;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.example.demo.dto.SessionAction;
import com.example.demo.dto.UserScoreUpdate;
import com.example.demo.utils.SourceDatabaseUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class SessionActionListener implements StepExecutionListener {
    
    private static final int USER_COUNT = 5;
    private static final int RECORD_COUNT = 20;
    private static final Random RANDOM = new Random();

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        
        initializeEmptyTables();
        prepareEmptyResultTable();

    }

    @Override
    @Nullable
    public ExitStatus afterStep(StepExecution stepExecution) {
        //fillBalanceProcessor.setStepExecution(null);
        return ExitStatus.COMPLETED;
    }

    void initializeEmptyTables() {
        SourceDatabaseUtils.dropTableIfExists(jdbcTemplate, SessionAction.SESSION_ACTION_TABLE_NAME);
        SourceDatabaseUtils.createSessionActionTable(jdbcTemplate, SessionAction.SESSION_ACTION_TABLE_NAME);

        for (int i = 0; i < RECORD_COUNT; i++) {
            SourceDatabaseUtils.insertSessionAction(jdbcTemplate, generateRecord(i + 1), SessionAction.SESSION_ACTION_TABLE_NAME);
        }
    }

    // Generate random session action record
    private static SessionAction generateRecord(long id) {
        long userId = 1 + RANDOM.nextInt(USER_COUNT);
        return RANDOM.nextBoolean()
                ? new SessionAction(id, userId, SourceDatabaseUtils.PLUS_TYPE, 1 + RANDOM.nextInt(3))
                : new SessionAction(id, userId, SourceDatabaseUtils.MULTI_TYPE, ((double) (1 + RANDOM.nextInt(5))) / 10 + 1d);
    }

    private void prepareEmptyResultTable() {
        SourceDatabaseUtils.dropTableIfExists(jdbcTemplate, UserScoreUpdate.USER_SCORE_TABLE_NAME);
        SourceDatabaseUtils.createUserScoreTable(jdbcTemplate, UserScoreUpdate.USER_SCORE_TABLE_NAME);
    }
    
}
