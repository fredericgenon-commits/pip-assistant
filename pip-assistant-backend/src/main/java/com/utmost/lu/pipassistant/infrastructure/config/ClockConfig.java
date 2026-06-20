package com.utmost.lu.pipassistant.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Provides the application {@link Clock}. Injecting a clock (instead of calling
 * {@code LocalDate.now()} directly) keeps time-dependent logic testable.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
