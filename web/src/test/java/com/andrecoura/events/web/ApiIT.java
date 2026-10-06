package com.andrecoura.events.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Base class: full application (all layers) against a real MySQL, driven through HTTP (MockMvc). */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ApiIT.CleanSchema.class)
abstract class ApiIT {

    @TestConfiguration
    static class CleanSchema {
        @Bean
        FlywayMigrationStrategy cleanMigrate() {
            return flyway -> {
                flyway.clean();
                flyway.migrate();
            };
        }
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void emptyTables() {
        jdbc.execute("DELETE FROM registrations");
        jdbc.execute("DELETE FROM participants");
        jdbc.execute("DELETE FROM events");
    }

    protected static final Instant START = Instant.now().plus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

    protected static String eventJson(String title, int capacity) {
        return eventJson(title, START, capacity);
    }

    protected static String eventJson(String title, Instant start, int capacity) {
        return """
                {"title":"%s","description":"d","venue":{"name":"Hall","address":"1 Street"},
                 "startsAt":"%s","endsAt":"%s","capacity":%d}"""
                .formatted(title, start, start.plus(2, ChronoUnit.HOURS), capacity);
    }

    protected ResultActions send(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request);
    }

    protected ResultActions postJson(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions getJson(String url) throws Exception {
        return mvc.perform(get(url));
    }

    protected String idOf(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id");
    }

    protected String createEvent(String title, int capacity, boolean publish) throws Exception {
        String id = idOf(postJson("/events", eventJson(title, capacity)).andExpect(status().isCreated()));
        if (publish) {
            postJson("/events/" + id + "/publish", "").andExpect(status().isOk());
        }
        return id;
    }

    protected String createParticipant(String name, String email) throws Exception {
        return idOf(postJson("/participants", "{\"name\":\"%s\",\"email\":\"%s\"}".formatted(name, email))
                .andExpect(status().isCreated()));
    }
}
