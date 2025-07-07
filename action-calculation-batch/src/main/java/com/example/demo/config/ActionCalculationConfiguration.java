package com.example.demo.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ActionCalculationConfiguration {


    @Bean
    @Qualifier("simpleActionCalculationJob")
    public Job simpleActionCalculationJob(JobRepository jobRepository,
        @Qualifier("simpleActionCalculationStep") Step simpleActionCalculationStep) {
            
        return new JobBuilder("simpleActionCalculationJob", jobRepository)
                .start(simpleActionCalculationStep)
                .build();
    }

    @Bean
    @Qualifier("multiThreadedActionCalculationJob")
    public Job multiThreadedActionCalculationJob(JobRepository jobRepository,
        @Qualifier("multiThreadedActionCalculationStep") Step multiThreadedActionCalculationStep) {
            
        return new JobBuilder("multiThreadedActionCalculationJob", jobRepository)
                .start(multiThreadedActionCalculationStep)
                .build();
    }

    @Bean
    @Qualifier("partitionedLocalActionCalculationJob")
    public Job partitionedLocalActionCalculationJob(JobRepository jobRepository,
        @Qualifier("partitionedLocalActionCalculationStep") Step partitionedLocalActionCalculationStep) {
            
        return new JobBuilder("partitionedLocalActionCalculationJob", jobRepository)
                .start(partitionedLocalActionCalculationStep)
                .build();
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
        threadPool.setCorePoolSize(3);
        return threadPool;
    }

}
