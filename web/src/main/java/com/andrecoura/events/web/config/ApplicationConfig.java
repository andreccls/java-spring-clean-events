package com.andrecoura.events.web.config;

import com.andrecoura.events.application.UseCase;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/** Wires the framework-free application layer into Spring: every {@link UseCase} class becomes a bean. */
@Configuration
@ComponentScan(
        basePackageClasses = UseCase.class,
        useDefaultFilters = false,
        includeFilters = @Filter(type = FilterType.ANNOTATION, classes = UseCase.class))
class ApplicationConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    OpenAPI openApi() {
        return new OpenAPI().info(new Info()
                .title("Events API")
                .version("1.0.0")
                .description("Reference project: Clean Architecture with Spring Boot. Not production-ready (no authentication)."));
    }
}
