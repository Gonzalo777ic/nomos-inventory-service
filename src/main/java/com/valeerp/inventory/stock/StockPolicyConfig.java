package com.valeerp.inventory.stock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StockQuantitySettings.class)
public class StockPolicyConfig {

    @Bean
    public StockQuantityPolicy stockQuantityPolicy(StockQuantitySettings settings) {
        return StockQuantityPolicy.from(settings);
    }
}
