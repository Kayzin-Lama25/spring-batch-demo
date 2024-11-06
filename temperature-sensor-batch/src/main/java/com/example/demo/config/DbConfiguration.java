// package com.example.demo.config;

// import javax.sql.DataSource;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.jdbc.datasource.DriverManagerDataSource;
// import org.springframework.jdbc.support.JdbcTransactionManager;
// import org.springframework.transaction.PlatformTransactionManager;

// @Configuration
// public class DbConfiguration {
    
//     @Bean
//     public DataSource dataSource(@Value("${spring.datasource.driver-class-name}") String driverClassName, 
//                                 @Value("${spring.datasource.url}") String url,
//                                 @Value("${spring.datasource.username}") String username,
//                                 @Value("${spring.datasource.password}") String password) {
//                                     DriverManagerDataSource dataSource = new DriverManagerDataSource();
//                                     dataSource.setDriverClassName(driverClassName);
//                                     dataSource.setUrl(url);
//                                     dataSource.setUsername(username);
//                                     dataSource.setPassword(password);
//                                     return dataSource;
//                                 }

//     @Bean
//     public PlatformTransactionManager transactionManager(DataSource dataSource) {
//         JdbcTransactionManager transactionManager = new JdbcTransactionManager();
//         transactionManager.setDataSource(dataSource);
//         return transactionManager;
//     }

//     // @Bean
//     // public BatchDataSourceScriptDatabaseInitializer batchDataSourceScriptDatabaseInitializer(DataSource dataSource, BatchProperties properties) {
//     //     return new BatchDataSourceScriptDatabaseInitializer(dataSource, properties.getJdbc());
//     // }

//     // @Bean
//     // public BatchProperties batchProperties(@Value("${batch.db.initialize-schema}") DatabaseInitializationMode initializationMode) {
//     //     BatchProperties batchProperties = new BatchProperties();
//     //     batchProperties.getJdbc().setInitializeSchema(initializationMode);
//     //     return batchProperties;
//     // }
// }
