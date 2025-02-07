package com.example.demo.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@Configuration
public class TemperatureRootConfiguration {

    @Value("classpath:input/HTE2NP.txt")
    private Resource rawDailyInputResource;

    @Value("file:HTE2NP.xml")
    private WritableResource aggregatedDailyOutputXmlResource;

    @Autowired
    Environment environment;

    @Bean
    public Job transactionJob(JobRepository jobRepository,
            @Qualifier("fillBalanceStep") Step fillBalanceStep) {

        return new JobBuilder("bankTransactionAnalysisJob", jobRepository)
                .start(fillBalanceStep)
                .build();
    }

    @Bean
    public Job currencyAdjustmentJob(JobRepository jobRepository,
            @Qualifier("currencyAdjustmentStep") Step currencyAdjustmentStep) {

        return new JobBuilder("currencyAdjustmentJob", jobRepository)
                .start(currencyAdjustmentStep)
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

    @Bean(name = "dataSource")
    @BatchDataSource
    public DataSource hsqlDatasource() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        EmbeddedDatabase embeddedDatabase = builder
            .addScript("classpath:org/springframework/batch/core/schema-hsqldb.sql")    // can add h2 db schema and type
            .setType(EmbeddedDatabaseType.HSQL)
            .build();
        return embeddedDatabase;
    }

}
