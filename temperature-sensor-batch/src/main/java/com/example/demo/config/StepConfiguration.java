package com.example.demo.config;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.demo.dto.DailyAggregatedSensorData;
import com.example.demo.dto.DailySensorData;
import com.example.demo.steps.RawToAggregateSensorDataProcessor;
import com.example.demo.steps.SensorDataTextMapper;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Configuration
public class StepConfiguration {
    
    @Value("classpath:input/HTE2NP.txt")
    private Resource rawDailyInputResource;

    @Value("file:HTE2NP.xml")
    private WritableResource aggregatedDailyOutputXmlResource;

    @Bean
    @Qualifier("aggregateSensorStep")
    public Step aggregateSensorStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("aggregateSensorStep", jobRepository)
            .<DailySensorData, DailyAggregatedSensorData>chunk(1, transactionManager)
            .reader(new FlatFileItemReaderBuilder<DailySensorData>()
                .name("dailySensorDataReader")
                .resource(new ClassPathResource("input/HTE2NP.txt"))
                .lineMapper(new SensorDataTextMapper()).build())
            .processor(new RawToAggregateSensorDataProcessor())
            .writer(new StaxEventItemWriterBuilder<DailyAggregatedSensorData>()
                .name("dailyAggregatedSensorDataWriter")
                .rootTagName("data")
                .marshaller(DailyAggregatedSensorData.getMarshaller())
                .resource(aggregatedDailyOutputXmlResource)
                .overwriteOutput(true).build())
            .build();
    }
}
