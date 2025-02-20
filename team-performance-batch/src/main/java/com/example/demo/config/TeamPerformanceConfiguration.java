package com.example.demo.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TeamPerformanceConfiguration {

    @Autowired
    Environment environment;

    @Bean
    @Qualifier("teamPerformanceJob")
    public Job teamPerformanceJob(JobRepository jobRepository,
            @Qualifier("threadPoolExecutor") TaskExecutor threadPoolExecutor,
            @Qualifier("averageTeamScoreStep") Step averageTeamScoreStep,
            @Qualifier("teamMaxRatioPerformanceStep") Step teamMaxRatioPerformanceStep,
            @Qualifier("teamMinRatioPerformanceStep") Step teamMinRatioPerformanceStep,
            @Qualifier("shellScriptStep") Step shellScriptStep) {

        Flow maxRatioPerformanceFlow = (Flow) new FlowBuilder<>("maxRatioPerformanceFlow")
                .start(teamMaxRatioPerformanceStep).build();

        Flow minRatioPerformanceFlow = (Flow) new FlowBuilder<>("minRatioPerformanceFlow")
                .start(teamMinRatioPerformanceStep).build();

        Flow performanceSplitFlow = (Flow) new FlowBuilder<>("performanceSplitFlow")
                .split(threadPoolExecutor)
                .add(maxRatioPerformanceFlow, minRatioPerformanceFlow)
                .build();

        return new JobBuilder("teamPerformanceJob", jobRepository)
                // 1. (Start) Flow with single step -> average team score
                // (flow is needed since the next is split flow, not a step)
                .start(new FlowBuilder<SimpleFlow>("averageTeamScoreFlow")
                        .start(averageTeamScoreStep)
                        .build())
                // 2. Next is parallel flow with 2 performance steps running in parallel
                .next(performanceSplitFlow)
                // 3. Execute shell script after done with parallel performance steps
                .next(shellScriptStep)
                .build()
                .build();
    }

    // @Bean
    // JdbcTemplate jdbcTemplate() {
    // return new JdbcTemplate();
    // }

    @Bean
    @Primary
    public DataSource postgresDataSource() {
        DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setUrl(environment.getProperty("spring.datasource.url"));
        driverManagerDataSource.setUsername(environment.getProperty("spring.datasource.username"));
        driverManagerDataSource.setPassword(environment.getProperty("spring.datasource.password"));
        return driverManagerDataSource;
    }

    /************ Spring Batch Initialization *******************/

    @Bean(name = "dataSource")
    @BatchDataSource
    public DataSource hsqlDatasource() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        EmbeddedDatabase embeddedDatabase = builder
                .addScript("classpath:org/springframework/batch/core/schema-hsqldb.sql") // can add h2 db schema and
                                                                                         // type
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
        return embeddedDatabase;
    }

    @Bean
    @Qualifier("asyncJobLauncher")
    public JobLauncher asyncJobLauncher(JobRepository jobRepository) {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return jobLauncher;
    }

    @Bean
    @Qualifier("threadPoolExecutor")
    public TaskExecutor threadPoolExecutor() {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.setCorePoolSize(2);
        return threadPool;
    }

}
