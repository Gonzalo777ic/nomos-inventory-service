package com.valeerp.inventory.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.valeerp.inventory.service", "com.valeerp"})
@EntityScan(basePackages = {"com.valeerp.inventory.service", "com.valeerp"})
@EnableJpaRepositories(basePackages = {"com.valeerp.inventory.service", "com.valeerp"})
public class ValeErpInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValeErpInventoryApplication.class, args);
    }

}
