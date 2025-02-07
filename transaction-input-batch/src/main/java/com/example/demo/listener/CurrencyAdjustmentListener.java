package com.example.demo.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class CurrencyAdjustmentListener implements StepExecutionListener {
    

    @SuppressWarnings("null")
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("stepExecution: {}", stepExecution);
        StepExecutionListener.super.beforeStep(stepExecution);
    }

    @SuppressWarnings("null")
    @Override
    @Nullable
    public ExitStatus afterStep(StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }

    
}
