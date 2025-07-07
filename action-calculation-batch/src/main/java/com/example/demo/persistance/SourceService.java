package com.example.demo.persistance;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SourceService {
    
    // Constants for action types
    public static final String PLUS_TYPE = "plus";
    public static final String MULTI_TYPE = "multi";


    public static void dropTableIfExists(JdbcTemplate jdbcTemplate, String tableName) {
        jdbcTemplate.update("drop table if exists " + tableName);
    }
}
