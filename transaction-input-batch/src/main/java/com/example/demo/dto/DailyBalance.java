package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.jdbc.core.RowMapper;

public class DailyBalance {

    private final int day;
    private final int month;
    private final BigDecimal balance;

    public DailyBalance(int day, int month, BigDecimal balance) {
        this.day = day;
        this.month = month;
        this.balance = balance;
    }

    public static final RowMapper<DailyBalance> ROW_MAPPER = (rs, rowNum) -> new DailyBalance(
            rs.getInt("day"),
            rs.getInt("month"),
            rs.getBigDecimal("balance"));

    public static PagingQueryProvider getPagingQueryProvider() {
        Map<String, Order> sortMap = new HashMap<>();
        sortMap.put("month", Order.ASCENDING);
        sortMap.put("day", Order.DESCENDING);
        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        queryProvider.setSelectClause("sum(amount) as balance, day, month");
        queryProvider.setFromClause("bank_transaction_yearly");
        queryProvider.setGroupClause("day, month");
        queryProvider.setSortKeys(sortMap);
        return queryProvider;
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public BigDecimal getBalance() {
        return balance;
    }

}
