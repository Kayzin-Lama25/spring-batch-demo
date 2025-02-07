package com.example.demo.processor;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import com.example.demo.dto.BalanceUpdate;
import com.example.demo.dto.BankTransaction;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FillBalanceProcessor implements ItemProcessor<BankTransaction, BalanceUpdate> {

    public static final String BALANCE_SO_FAR = "balanceSoFar";

    private StepExecution stepExecution;

    @Override
    @Nullable
    public BalanceUpdate process(@NonNull BankTransaction item) throws Exception {

        if (stepExecution == null) {
            throw new RuntimeException("Cannot process item without accessing the step execution");
        }

        BigDecimal newBalance = BigDecimal.valueOf(getLatestTransactionBalance())
                        .setScale(2, RoundingMode.HALF_UP)
                        .add(item.getAmount());
        
        BalanceUpdate balanceUpdate = new BalanceUpdate(item.getId(), newBalance);
        stepExecution.getExecutionContext().putDouble(BALANCE_SO_FAR, newBalance.doubleValue());
        return balanceUpdate;
    }

    public double getLatestTransactionBalance() {
        if (stepExecution == null) {
            throw new RuntimeException("Cannot get the latest balance without accessing the step execution.");
        }

        return stepExecution.getExecutionContext().getDouble(BALANCE_SO_FAR, 0d);
    }

    public void setStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }
    
}
