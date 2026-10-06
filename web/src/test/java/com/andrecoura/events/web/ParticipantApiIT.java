package com.andrecoura.events.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ParticipantApiIT extends ApiIT {

    @Test
    void createsGetsAndNormalizesEmail() throws Exception {
        String id = createParticipant("Maria", "Maria@Example.com");

        getJson("/participants/" + id).andExpect(status().isOk()).andExpect(jsonPath("$.email").value("maria@example.com"));
    }

    @Test
    void duplicateEmailIs409() throws Exception {
        createParticipant("Maria", "maria@example.com");

        postJson("/participants", "{\"name\":\"Other\",\"email\":\"MARIA@example.com\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    void invalidInputIs400() throws Exception {
        postJson("/participants", "{\"name\":\"\",\"email\":\"x\"}").andExpect(status().isBadRequest());
        postJson("/participants", "{\"name\":\"Ana\",\"email\":\"not-an-email\"}").andExpect(status().isBadRequest());
    }

    @Test
    void updatesListsAndDeletes() throws Exception {
        String id = createParticipant("Maria", "maria@example.com");

        send(put("/participants/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Maria S.\",\"email\":\"maria@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria S."));
        getJson("/participants").andExpect(jsonPath("$.total").value(1));
        send(delete("/participants/" + id)).andExpect(status().isNoContent());
        getJson("/participants/" + id).andExpect(status().isNotFound());
        getJson("/participants/" + UUID.randomUUID()).andExpect(status().isNotFound());
    }
}
