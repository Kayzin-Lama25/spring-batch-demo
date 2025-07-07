package com.example.demo.api;

import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UserScoreUpdate;
import com.example.demo.utils.SourceDatabaseUtils;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@RequiredArgsConstructor
@RestController
public class ApplicationController {

    @Qualifier("asyncJobLauncher")
    private final JobLauncher jobLauncher;

    @Qualifier("simpleActionCalculationJob")
    private final Job simpleActionCalculationJob;

    @Qualifier("multiThreadedActionCalculationJob")
    private final Job multiThreadedActionCalculationJob;

    @Qualifier("partitionedLocalActionCalculationJob")
    private final Job partitionedLocalActionCalculationJob;
    
    @GetMapping("/start-simple-local")
    public String startSimpleLocal() throws Exception {
        jobLauncher.run(simpleActionCalculationJob, buildUniqueJobParameters());
        return "Successfully started";
    }

    @GetMapping("/start-multi-threaded")
    public String startMultiThreaded() throws Exception {
        jobLauncher.run(multiThreadedActionCalculationJob, buildUniqueJobParameters());
        return "Successfully started";
    }

    @GetMapping("/start-partitioned-local")
    public String startPartitionedLocal() throws Exception {
        jobLauncher.run(partitionedLocalActionCalculationJob, buildUniqueJobParameters());
        return "Successfully started";
    }

    // Building unique job parameters to not care about restarts
    private static JobParameters buildUniqueJobParameters() {
        return new JobParametersBuilder()
                .addString(UUID.randomUUID().toString(), UUID.randomUUID().toString())
                .toJobParameters();
    }
    
}
