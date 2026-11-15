package com.estantedigital.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Configuration
public class MetricsConfig {

    @Bean
    Counter salesCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("estantedigital.sales.created")
                .description("Number of successfully registered sales")
                .register(meterRegistry);
    }
}
