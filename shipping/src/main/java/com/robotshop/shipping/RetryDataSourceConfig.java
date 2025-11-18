package com.robotshop.shipping;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetryDataSourceConfig {

    @Bean
    public DataSource retryableDataSource(DataSource dataSource) {
        return new RetryableDataSource(dataSource);
    }
}