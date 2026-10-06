package com.andrecoura.events.web.event;

import com.andrecoura.events.application.event.EventData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/** HTTP payload for creating/replacing an event. Bean Validation checks the shape; the domain owns the rules. */
record EventRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @NotNull @Valid VenueRequest venue,
        @NotNull Instant startsAt,
        @NotNull Instant endsAt,
        @NotNull @Min(0) Integer capacity) {

    record VenueRequest(@NotBlank @Size(max = 120) String name, @NotBlank @Size(max = 255) String address) {}

    EventData toData() {
        return new EventData(title, description, venue.name(), venue.address(), startsAt, endsAt, capacity);
    }
}
