package com.andrecoura.events.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class EventApiIT extends ApiIT {

    @Test
    void createsAndFetchesAnEvent() throws Exception {
        postJson("/events", eventJson("Meetup", 50))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/events/")))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.venue.name").value("Hall"));
        getJson("/events").andExpect(status().isOk()).andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void fieldValidationReturnsProblemDetailsWithErrors() throws Exception {
        postJson("/events", "{\"title\":\"\",\"capacity\":-1}")
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors[?(@.field=='title')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field=='venue')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field=='capacity')]").exists());
    }

    @Test
    void domainRuleViolationIs400() throws Exception {
        postJson("/events", eventJson("Backwards", START, 5).replace("\"endsAt\":\"" + START.plus(2, ChronoUnit.HOURS) + "\"",
                        "\"endsAt\":\"" + START.minus(1, ChronoUnit.HOURS) + "\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Business rule violated"))
                .andExpect(jsonPath("$.detail").value("end must be after start"));
    }

    @Test
    void malformedJsonAndBadIdsAre400() throws Exception {
        postJson("/events", "{not json").andExpect(status().isBadRequest());
        getJson("/events/not-a-uuid").andExpect(status().isBadRequest());
        getJson("/events?status=NOPE").andExpect(status().isBadRequest());
    }

    @Test
    void unknownEventIs404ProblemDetails() throws Exception {
        getJson("/events/" + java.util.UUID.randomUUID())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not found"));
    }

    @Test
    void publishRulesAndLifecycle() throws Exception {
        String noCapacity = createEvent("Zero", 0, false);
        postJson("/events/" + noCapacity + "/publish", "").andExpect(status().isBadRequest());

        String id = createEvent("Normal", 10, true);
        postJson("/events/" + id + "/publish", "").andExpect(status().isConflict());
        postJson("/events/" + id + "/finish", "").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FINISHED"));
        postJson("/events/" + id + "/cancel", "").andExpect(status().isConflict());
    }

    @Test
    void cancelledEventCannotBeEdited() throws Exception {
        String id = createEvent("To cancel", 10, true);
        postJson("/events/" + id + "/cancel", "").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));

        send(put("/events/" + id).contentType(MediaType.APPLICATION_JSON).content(eventJson("New", 10)))
                .andExpect(status().isConflict());
    }

    @Test
    void updatesAnEvent() throws Exception {
        String id = createEvent("Old", 10, false);

        send(put("/events/" + id).contentType(MediaType.APPLICATION_JSON).content(eventJson("New", 20)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New"))
                .andExpect(jsonPath("$.capacity").value(20));
    }

    @Test
    void deletesOnlyDrafts() throws Exception {
        String draft = createEvent("Draft", 10, false);
        String published = createEvent("Published", 10, true);

        send(delete("/events/" + draft)).andExpect(status().isNoContent());
        send(delete("/events/" + published)).andExpect(status().isConflict());
        getJson("/events/" + draft).andExpect(status().isNotFound());
    }

    @Test
    void filtersByStatusAndPeriodAndPaginates() throws Exception {
        String a = createEvent("A", 10, true);
        createEvent("B", START.plus(5, ChronoUnit.DAYS), 10);
        createEvent("C", START.plus(10, ChronoUnit.DAYS), 10);

        getJson("/events?status=PUBLISHED").andExpect(jsonPath("$.total").value(1)).andExpect(jsonPath("$.items[0].id").value(a));
        getJson("/events?from=" + START.plus(4, ChronoUnit.DAYS) + "&to=" + START.plus(6, ChronoUnit.DAYS))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].title").value("B"));
        getJson("/events?page=2&pageSize=2")
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].title").value("C"))
                .andExpect(jsonPath("$.total").value(3));
    }

    private String createEvent(String title, java.time.Instant start, int capacity) throws Exception {
        return idOf(postJson("/events", eventJson(title, start, capacity)).andExpect(status().isCreated()));
    }
}
