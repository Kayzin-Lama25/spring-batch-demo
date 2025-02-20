package com.example.demo.api;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;



@RestController
@RequestMapping("/api")
public class ApplicationApi {
    
    @Autowired
    @Qualifier("asyncJobLauncher")
    JobLauncher jobLauncher;

    @Autowired
    @Qualifier("teamPerformanceJob")
    private Job teamPerformanceJob;

    @GetMapping("/start")
    public String start(@RequestParam int scoreRank) {
        String uuid = UUID.randomUUID().toString();
        launchJobAsynchronously(scoreRank, uuid);
        return "Job with id " + uuid + " was submitted";
    }

    private void launchJobAsynchronously(int scoreRank, String uuid) {
        try {
            jobLauncher.run(teamPerformanceJob, new JobParametersBuilder()
                .addLong("scoreRank", (long)scoreRank)
                .addString("uuid", uuid).toJobParameters());
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException e) {
            e.printStackTrace();
        }
    }
    
}
