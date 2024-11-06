
package com.example.demo.steps;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.batch.item.file.LineMapper;
import org.springframework.stereotype.Component;

import com.example.demo.dto.DailySensorData;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class SensorDataTextMapper implements LineMapper<DailySensorData> {

    @Override
    public DailySensorData mapLine(String line, int lineNumber) throws Exception {
        log.info("line: {}", line);
        log.info("lineNumber: {}", lineNumber);
        String[] dateAndMeasurements = line.split(":");
        return new DailySensorData(dateAndMeasurements[0], 
            Arrays.stream(dateAndMeasurements[1].split(","))
            .map(Double::parseDouble)
            .collect(Collectors.toList()));
    }

}