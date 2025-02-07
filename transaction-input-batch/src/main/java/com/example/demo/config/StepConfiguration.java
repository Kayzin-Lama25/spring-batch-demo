package com.example.demo.config;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.sql.DataSource;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.demo.dto.BalanceUpdate;
import com.example.demo.dto.BankTransaction;
import com.example.demo.dto.CurrencyAdjustment;
import com.example.demo.listener.BankTransactionListener;
import com.example.demo.listener.CurrencyAdjustmentListener;
import com.example.demo.processor.FillBalanceProcessor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class StepConfiguration {

    @Value("${currency.adjustment.rate}")
    double rate;

    @Value("${currency.adjustment.disallowed.merchant}")
    String disallowedMerchant;

    @Bean
    @Qualifier("fillBalanceStep")
    public Step fillBalanceStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            @Qualifier("postgresDataSource") DataSource dataSource, JdbcTemplate jdbcTemplate,
            FillBalanceProcessor fillBalanceProcessor) {
        log.info("dataSource: {}", dataSource);
        // FillBalanceProcessor processor = new FillBalanceProcessor();
        return new StepBuilder("fill-balance", jobRepository)
                .<BankTransaction, BalanceUpdate>chunk(4, transactionManager)
                .reader(new JdbcCursorItemReaderBuilder<BankTransaction>()
                        .dataSource(dataSource)
                        .name("bankTransactionReader")
                        .sql(BankTransaction.SELECT_ALL_QUERY)
                        .rowMapper(BankTransaction.ROW_MAPPER).build())
                .processor(fillBalanceProcessor)
                .writer(new JdbcBatchItemWriterBuilder<BalanceUpdate>()
                        .dataSource(dataSource)
                        .sql("update bank_transaction_yearly set balance = ? where id = ?")
                        .itemPreparedStatementSetter((item, ps) -> {
                            ps.setBigDecimal(1, item.getBalance());
                            ps.setLong(2, item.getId());
                        }).build())
                .listener(new BankTransactionListener(jdbcTemplate, fillBalanceProcessor))
                .build();
    }

    @Bean
    FillBalanceProcessor fillBalanceProcessor() {
        return new FillBalanceProcessor();
    }

    @Bean
    @Qualifier("currencyAdjustmentStep")
    public Step currencyAdjustmentStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
    @Qualifier("postgresDataSource") DataSource dataSource, JdbcTemplate jdbcTemplate) {
        return new StepBuilder("currency-adjustment", jobRepository)
            .<BankTransaction, CurrencyAdjustment>chunk(1, platformTransactionManager)
            .reader(new JdbcCursorItemReaderBuilder<BankTransaction>()
                    .dataSource(dataSource)
                    .name("bankTransactionReader")
                    .sql(BankTransaction.SELECT_ALL_QUERY + " WHERE adjusted=false;")
                    .rowMapper(BankTransaction.ROW_MAPPER)
                    .saveState(false)
                    .build())
            .processor(item -> {
                CurrencyAdjustment adjustment = new CurrencyAdjustment();
                adjustment.id = item.getId();
                adjustment.adjustedAmount = item.getAmount()
                    .multiply(BigDecimal.valueOf(rate))
                    .setScale(2, RoundingMode.HALF_UP);
                return adjustment;
            })
            .writer(new JdbcBatchItemWriterBuilder<CurrencyAdjustment>()
                .dataSource(dataSource)
                .sql("update bank_transaction_yearly set amount = ?, adjusted = ? where id = ?")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setBigDecimal(1, item.adjustedAmount);
                    ps.setBoolean(2, true);
                    ps.setLong(3, item.id);
                }).build())
            .listener(new CurrencyAdjustmentListener())
            // always restarts step, regardless of whether same parameters step was completed
            .allowStartIfComplete(true)
            .build();
    }
}
