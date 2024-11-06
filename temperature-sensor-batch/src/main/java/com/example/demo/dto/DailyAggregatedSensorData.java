package com.example.demo.dto;

import java.util.HashMap;
import java.util.Map;

import org.springframework.oxm.xstream.XStreamMarshaller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@Component
public class DailyAggregatedSensorData {

    private String date;
    private double min;
    private double avg;
    private double max;

    public static final String ITEM_ROOT_ELEMENT_NAME = "daily-data";

    public DailyAggregatedSensorData(String date, double min, double avg, double max) {
        this.date = date;
        this.min = min;
        this.max = max;
        this.avg = avg;
    }

    public static XStreamMarshaller getMarshaller() {
        XStreamMarshaller marshaller = new XStreamMarshaller();

        Map<String, Class<?>> aliases = new HashMap<>();
        aliases.put(ITEM_ROOT_ELEMENT_NAME, DailyAggregatedSensorData.class);
        aliases.put("date", String.class);
        aliases.put("min", Double.class);
        aliases.put("avg", Double.class);
        aliases.put("max", Double.class);

        //ExplicitTypePermission
        marshaller.setAliases(aliases);

        return marshaller;
    }
}
