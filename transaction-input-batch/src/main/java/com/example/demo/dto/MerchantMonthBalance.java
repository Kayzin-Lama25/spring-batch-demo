package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.jdbc.core.RowMapper;

public class MerchantMonthBalance {
    
    private final int month;
    private final String merchant;
    private final BigDecimal balance;

    public static final RowMapper<MerchantMonthBalance> ROW_MAPPER = (rs, rowNum) -> new MerchantMonthBalance(
        rs.getInt("month"),
        rs.getString("merchant"),
        rs.getBigDecimal("balance")
    );

    public static PagingQueryProvider getPagingQueryProvider() {
        Map<String, Order> sortMap = new HashMap<>();
        sortMap.put("month", Order.ASCENDING);
        sortMap.put("merchant", Order.DESCENDING);
        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        queryProvider.setSelectClause("sum(amount) as balance, merchant, month");
        queryProvider.setFromClause("bank_transaction_yearly");
        queryProvider.setGroupClause("month, merchant");
        queryProvider.setSortKeys(sortMap);
        return queryProvider;
    }

    public MerchantMonthBalance(int month, String merchant, BigDecimal balance) {
        this.month = month;
        this.merchant = merchant;
        this.balance = balance;
    }

    public int getMonth() {
        return month;
    }

    public String getMerchant() {
        return merchant;
    }

    public BigDecimal getBalance() {
        return balance;
    }

}
