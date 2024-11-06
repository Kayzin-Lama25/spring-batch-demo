package com.example.demo.steps;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.example.demo.constants.AnomalyType;
import com.example.demo.dto.DailyAggregatedSensorData;
import com.example.demo.dto.DataAnomaly;

@Component
public class SensorDataAnomalyProcessor implements ItemProcessor<DailyAggregatedSensorData, DataAnomaly> {

    private static final double THRESHOLD = 0.9;

    @Override
    public DataAnomaly process(DailyAggregatedSensorData item) throws Exception {
        if ((item.getMin() / item.getAvg()) < THRESHOLD) {
            return new DataAnomaly(item.getDate(), AnomalyType.MINIMUM, item.getMin());
        } else if ((item.getAvg() / item.getMax()) < THRESHOLD) {
            return new DataAnomaly(item.getDate(), AnomalyType.MAXIMUM, item.getMax());
        } else {
            return null;
        }
    }
    
}
