package com.andrecoura.events.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegistrationApiIT extends ApiIT {

    String body(String participantId) {
        return "{\"participantId\":\"%s\"}".formatted(participantId);
    }

    @Test
    void registersAParticipantInAPublishedEvent() throws Exception {
        String event = createEvent("Meetup", 2, true);
        String maria = createParticipant("Maria", "maria@example.com");

        postJson("/events/" + event + "/registrations", body(maria))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.eventId").value(event));
        getJson("/events/" + event + "/registrations").andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void fullEventAndDuplicateAreConflicts() throws Exception {
        String event = createEvent("Tiny", 1, true);
        String maria = createParticipant("Maria", "maria@example.com");
        String joao = createParticipant("Joao", "joao@example.com");
        postJson("/events/" + event + "/registrations", body(maria)).andExpect(status().isCreated());

        postJson("/events/" + event + "/registrations", body(maria))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("participant is already registered in this event"));
        postJson("/events/" + event + "/registrations", body(joao))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("event is full"));
    }

    @Test
    void cancellingFreesTheSeat() throws Exception {
        String event = createEvent("Tiny", 1, true);
        String maria = createParticipant("Maria", "maria@example.com");
        String joao = createParticipant("Joao", "joao@example.com");
        String seat = idOf(postJson("/events/" + event + "/registrations", body(maria)).andExpect(status().isCreated()));

        postJson("/registrations/" + seat + "/cancel", "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        postJson("/registrations/" + seat + "/cancel", "").andExpect(status().isConflict());
        postJson("/events/" + event + "/registrations", body(joao)).andExpect(status().isCreated());
        getJson("/events/" + event + "/registrations").andExpect(jsonPath("$.total").value(2));
    }

    @Test
    void draftEventDoesNotAcceptRegistrations() throws Exception {
        String draft = createEvent("Draft", 5, false);
        String maria = createParticipant("Maria", "maria@example.com");

        postJson("/events/" + draft + "/registrations", body(maria))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("registrations are only accepted for published events"));
    }

    @Test
    void unknownReferencesAre404AndMissingBodyFieldIs400() throws Exception {
        String event = createEvent("Meetup", 5, true);

        postJson("/events/" + event + "/registrations", body(UUID.randomUUID().toString())).andExpect(status().isNotFound());
        postJson("/events/" + UUID.randomUUID() + "/registrations", body(UUID.randomUUID().toString())).andExpect(status().isNotFound());
        postJson("/events/" + event + "/registrations", "{}").andExpect(status().isBadRequest());
        postJson("/registrations/" + UUID.randomUUID() + "/cancel", "").andExpect(status().isNotFound());
        getJson("/events/" + UUID.randomUUID() + "/registrations").andExpect(status().isNotFound());
    }

    @Test
    void participantWithRegistrationsCannotBeDeleted() throws Exception {
        String event = createEvent("Meetup", 5, true);
        String maria = createParticipant("Maria", "maria@example.com");
        postJson("/events/" + event + "/registrations", body(maria)).andExpect(status().isCreated());

        send(delete("/participants/" + maria)).andExpect(status().isConflict());
    }
}
