package com.example.demo.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.demo.dto.SessionAction;
import com.example.demo.dto.UserScoreUpdate;
import com.example.demo.steps.listeners.SessionActionListener;
import com.example.demo.steps.partitioners.SessionActionPartitioner;
import com.example.demo.utils.SourceDatabaseUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class StepConfiguration {

    @Autowired
    private SessionActionListener sessionActionListener;

    @Bean
    @Qualifier("simpleActionCalculationStep")
    public Step simpleActionCalculationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            @Qualifier("sessionActionReader") ItemReader<SessionAction> sessionItemReader,
            @Qualifier("postgresDataSource") DataSource dataSource) {
        return new StepBuilder("simpleActionCalculationStep", jobRepository)
                .<SessionAction, UserScoreUpdate>chunk(1, transactionManager)
                .reader(sessionItemReader)
                .processor(getSessionActionProcessor())
                .writer(new JdbcBatchItemWriterBuilder<UserScoreUpdate>()
                        .dataSource(dataSource)
                        .itemPreparedStatementSetter(SourceDatabaseUtils.UPDATE_USER_SCORE_PARAMETER_SETTER)
                        .sql(SourceDatabaseUtils.constructUpdateUserScoreQuery(UserScoreUpdate.USER_SCORE_TABLE_NAME))
                        .build())
                .listener(sessionActionListener)
                .listener(beforeStepLoggerListener())
                .build();
    }

    @Bean
    @Qualifier("multiThreadedActionCalculationStep")
    public Step multiThreadedActionCalculationStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("sessionActionReader") ItemStreamReader<SessionAction> sessionActionReader,
            @Qualifier("postgresDataSource") DataSource dataSource) {
        return new StepBuilder("multiThreadedActionCalculationStep", jobRepository)
                .<SessionAction, UserScoreUpdate>chunk(5, transactionManager)
                .reader(new SynchronizedItemStreamReaderBuilder<SessionAction>()
                        .delegate(sessionActionReader)
                        .build())
                .processor(getSessionActionProcessor())
                .writer(new JdbcBatchItemWriterBuilder<UserScoreUpdate>()
                        .dataSource(dataSource)
                        .itemPreparedStatementSetter(SourceDatabaseUtils.UPDATE_USER_SCORE_PARAMETER_SETTER)
                        .sql(SourceDatabaseUtils.constructUpdateUserScoreQuery(UserScoreUpdate.USER_SCORE_TABLE_NAME))
                        .build())
                .listener(sessionActionListener)
                .listener(beforeStepLoggerListener())
                .build();

    }

    @Bean
    @Qualifier("partitionedLocalActionCalculationStep")
    public Step partitionedLocalActionCalculationStep(JobRepository jobRepository,
            @Qualifier("simplePartitionActionCalculationStep")Step simplePartitionActionCalculationStep) {
        return new StepBuilder("partitionedLocalActionCalculationStep", jobRepository)
                .partitioner("simplePartitionActionCalculationStep", new SessionActionPartitioner())
                .step(simplePartitionActionCalculationStep)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .gridSize(3)    // 3 threads to partition data handling
                .listener(sessionActionListener)
                .build();
    }

    @Bean
    @Qualifier("simplePartitionActionCalculationStep")
    public Step simplePartitionActionCalculationStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
            @Qualifier("sessionActionReader") ItemReader<SessionAction> sessionItemReader,
            @Qualifier("postgresDataSource") DataSource dataSource) {
        return new StepBuilder("simplePartitionActionCalculationStep", jobRepository)
                .<SessionAction, UserScoreUpdate>chunk(1, transactionManager)
                .reader(sessionItemReader)
                .processor(getSessionActionProcessor())
                .writer(new JdbcBatchItemWriterBuilder<UserScoreUpdate>()
                        .dataSource(dataSource)
                        .itemPreparedStatementSetter(SourceDatabaseUtils.UPDATE_USER_SCORE_PARAMETER_SETTER)
                        .sql(SourceDatabaseUtils.constructUpdateUserScoreQuery(UserScoreUpdate.USER_SCORE_TABLE_NAME))
                        .build())
                .listener(beforeStepLoggerListener())
                .build();
    }

    @Bean
    @StepScope
    @Qualifier("sessionActionReader")
    public ItemStreamReader<SessionAction> sessionActionReader(@Qualifier("postgresDataSource") DataSource dataSource,
            @Value("#{stepExecutionContext['partitionCount']}") Integer partitionCount,
            @Value("#{stepExecutionContext['partitionIndex']}") Integer partitionIndex) {
        PagingQueryProvider queryProvider = (partitionCount == null || partitionIndex == null)
                ? SourceDatabaseUtils.selectAllSessionActionsProvider(SessionAction.SESSION_ACTION_TABLE_NAME)
                : SourceDatabaseUtils.selectPartitionOfSessionActionsProvider(SessionAction.SESSION_ACTION_TABLE_NAME,
                        partitionCount, partitionIndex);
        return new JdbcPagingItemReaderBuilder<SessionAction>()
                .name("sessionActionReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider)
                .rowMapper(SourceDatabaseUtils.getSessionActionMapper())
                .pageSize(5).build();
    }

    private static ItemProcessor<SessionAction, UserScoreUpdate> getSessionActionProcessor() {
        return sessionAction -> {
            if (SourceDatabaseUtils.PLUS_TYPE.equals(sessionAction.getActionType())) {
                return new UserScoreUpdate(sessionAction.getUserId(), sessionAction.getAmount(), 1d);
            } else if (SourceDatabaseUtils.MULTI_TYPE.equals(sessionAction.getActionType())) {
                return new UserScoreUpdate(sessionAction.getUserId(), 0d, sessionAction.getAmount());
            } else {
                throw new RuntimeException("Unknown session record type: " + sessionAction.getActionType());
            }
        };
    }

    private static StepExecutionListener beforeStepLoggerListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                int partitionCount = stepExecution.getExecutionContext()
                        .getInt(SessionActionPartitioner.PARTITION_COUNT, -1);
                int partitionIndex = stepExecution.getExecutionContext()
                        .getInt(SessionActionPartitioner.PARTITION_INDEX, -1);
                if (partitionIndex == -1 || partitionCount == -1) {
                    log.info("Calculation step is about to start handling all session action records");
                } else {
                    String threadName = Thread.currentThread().getName();
                    log.info("Calculation step is about to start handling partition " + partitionIndex
                            + " out of total " + partitionCount + " partitions in the thread -> " + threadName);
                }
            }
        };
    }
}
