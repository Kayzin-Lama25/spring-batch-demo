package com.example.demo.listener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.example.demo.dto.BankTransaction;
import com.example.demo.processor.FillBalanceProcessor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class BankTransactionListener implements StepExecutionListener {

    // Number of records to generate
    private static final int TARGET_RECORD_NUM = 300;
    // Number of unique merchants to be used in generated records
    private static final int TARGET_UNIQUE_MERCHANT_NUM = 40;
    // Key is month number, 1-indexed, i.e. 1 is January, 12 is December; value is the number of days
    private static final Map<Integer, Integer> DAYS_IN_MONTH_MAP = new HashMap<>() {{
        put(1, 31);
        put(2, 28);
        put(3, 31);
        put(4, 30);
        put(5, 31);
        put(6, 30);
        put(7, 31);
        put(8, 31);
        put(9, 30);
        put(10, 31);
        put(11, 30);
        put(12, 31);
    }};

    private final JdbcTemplate jdbcTemplate;
    private final FillBalanceProcessor fillBalanceProcessor;

    @Override
    public void beforeStep(StepExecution stepExecution) {

        initializeEmptyDatabase();

        List<BankTransaction> recordsToInsert = new ArrayList<>(TARGET_RECORD_NUM);
        Random random = new Random();
        String[] merchants = generateMerchants();
        for (int i = 0; i < TARGET_RECORD_NUM; i++) {
            recordsToInsert.add(generateRecord(random, merchants));
        }

        // Sort random records chronologically
        recordsToInsert.sort((t1, t2) -> {
            if (t1.getMonth() < t2.getMonth()) {
                return -1;
            } else if (t1.getMonth() > t2.getMonth()) {
                return 1;
            } else if (t1.getDay() < t2.getDay()) {
                return -1;
            } else if (t1.getDay() > t2.getDay()) {
                return 1;
            } else if (t1.getHour() < t2.getHour()) {
                return -1;
            } else if (t1.getHour() > t2.getHour()) {
                return 1;
            } else if (t1.getMinute() < t2.getMinute()) {
                return -1;
            } else if (t1.getMinute() > t2.getMinute()) {
                return 1;
            } else {
                return t1.getAmount().compareTo(t2.getAmount());
            }
        });

        for (BankTransaction transaction : recordsToInsert) {
            // Insert records in the db, relying on auto-increment (serial) for id
            jdbcTemplate.update("insert into bank_transaction_yearly (month, day, hour, minute, amount, merchant, adjusted, balance) " +
                        "values (?, ?, ?, ?, ?, ?, ?, ?)",
                transaction.getMonth(), transaction.getDay(), transaction.getHour(),
                transaction.getMinute(), transaction.getAmount(), transaction.getMerchant(), false, null);
        }

        // Print to console the success message
        System.out.println("Input source table with " + TARGET_RECORD_NUM + " records is successfully initialized");

        System.out.println("fillBalanceProcessor: " + fillBalanceProcessor);
        fillBalanceProcessor.setStepExecution(stepExecution);
    }

    @Override
    @Nullable
    public ExitStatus afterStep(StepExecution stepExecution) {
        fillBalanceProcessor.setStepExecution(null);
        return ExitStatus.COMPLETED;
    }

    // Return array of merchant names to be used
    private String[] generateMerchants() {
        String[] merchantsArray = new String[TARGET_UNIQUE_MERCHANT_NUM];
        for (int i = 0; i < TARGET_UNIQUE_MERCHANT_NUM; i++) {
            merchantsArray[i] = UUID.randomUUID().toString();
        }
        return merchantsArray;
    }
    
    // Generate random transaction record using pre-calculated list of merchants to use
    public BankTransaction generateRecord(Random random, String[] merchants) {
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(DAYS_IN_MONTH_MAP.get(month)) + 1;
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        double doubleAmount = ((double) random.nextInt(100000)) / 100;
        if (random.nextBoolean()) {
            doubleAmount *= -1;
        }
        BigDecimal amount = new BigDecimal(doubleAmount).setScale(2, RoundingMode.HALF_UP);
        String merchant = merchants[random.nextInt(merchants.length)];

        return new BankTransaction(-1, month, day, hour, minute, amount, merchant);
    }
    
    public  void initializeEmptyDatabase() {

        jdbcTemplate.update("DROP TABLE IF EXISTS public.bank_transaction_yearly");

        jdbcTemplate.update("CREATE TABLE IF NOT EXISTS public.bank_transaction_yearly (" +
            "id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY, " +
            "month integer NOT NULL," +
            "day integer NOT NULL," +
            "hour integer NOT NULL," +
            "minute integer NOT NULL," +
            "amount numeric(10,2) NOT NULL," +
            "merchant varchar(36) NOT NULL," +
            "adjusted boolean," +
            "balance numeric(10,2) NULL, " +
            "CONSTRAINT bank_transaction_yearly_pkey PRIMARY KEY (id)" +
            ")");
    }


}
