package com.andrecoura.events.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class PlatformIT extends ApiIT {

    @Test
    void healthIsUpAndIncludesTheDatabase() throws Exception {
        getJson("/actuator/health").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void openApiDocumentDescribesTheEndpoints() throws Exception {
        getJson("/v3/api-docs")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Events API"))
                .andExpect(jsonPath("$.paths['/events']").exists())
                .andExpect(jsonPath("$.paths['/events/{eventId}/registrations']").exists());
    }
}
