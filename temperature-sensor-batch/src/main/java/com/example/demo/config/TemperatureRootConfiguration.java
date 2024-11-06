package com.example.demo.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

@Configuration
public class TemperatureRootConfiguration {

    @Value("classpath:input/HTE2NP.txt")
    private Resource rawDailyInputResource;

    @Value("file:HTE2NP.xml")
    private WritableResource aggregatedDailyOutputXmlResource;
    
    @Bean
    public Job temperatureSensorJob(JobRepository jobRepository,
        @Qualifier("aggregateSensorStep") Step aggregateSensorStep) {
            
        return new JobBuilder("temperatureSensorJob", jobRepository)
                .start(aggregateSensorStep)
                .build();
    }

}
