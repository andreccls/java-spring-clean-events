package com.andrecoura.events.infrastructure;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/** Minimal Spring Boot context so the adapters can be tested without the web module. */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import(InfrastructureConfig.class)
public class TestApplication {

    /** Always start from a clean schema so the real migrations are exercised on every run. */
    @Bean
    FlywayMigrationStrategy cleanMigrate() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }
}
