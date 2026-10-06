package com.andrecoura.events.web;

import com.andrecoura.events.infrastructure.InfrastructureConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/** Composition root: the only place that knows every layer. */
@SpringBootApplication
@Import(InfrastructureConfig.class)
public class EventsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventsApplication.class, args);
    }
}
