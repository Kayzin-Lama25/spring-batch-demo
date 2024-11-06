package com.example.demo.steps;

import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.example.demo.dto.DailyAggregatedSensorData;
import com.example.demo.dto.DailySensorData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RawToAggregateSensorDataProcessor implements ItemProcessor<DailySensorData, DailyAggregatedSensorData> {

    @Override
    public DailyAggregatedSensorData process(DailySensorData item) throws Exception {
        log.info("start processing....");
        double min = item.getMeasurements().get(0);
        double max = min;
        double sum = 0;

        for (double measurement: item.getMeasurements()) {
            min = Math.min(min, measurement);
            max = Math.max(max, measurement);
            sum += measurement;
        }

        double avg = sum / item.getMeasurements().size();

        return new DailyAggregatedSensorData(item.getDate(), convertToCelsius(min), convertToCelsius(avg), convertToCelsius(max));
    }

    public static double convertToCelsius(double fahT) {
        return (5 * (fahT - 32)) / 9;
    }

    
}