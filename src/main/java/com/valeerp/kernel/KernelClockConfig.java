package com.valeerp.kernel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class KernelClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
