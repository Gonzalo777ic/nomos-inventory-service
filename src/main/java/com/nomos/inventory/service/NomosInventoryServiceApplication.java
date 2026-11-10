package com.nomos.inventory.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.nomos.inventory.service", "com.valeerp"})
@EntityScan(basePackages = {"com.nomos.inventory.service", "com.valeerp"})
@EnableJpaRepositories(basePackages = {"com.nomos.inventory.service", "com.valeerp"})
public class NomosInventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NomosInventoryServiceApplication.class, args);
    }

}
