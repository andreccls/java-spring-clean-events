package com.andrecoura.events.application.event;

import com.andrecoura.events.domain.event.Venue;
import java.time.Instant;

/** Editable fields of an event, shared by the create and update commands. */
public record EventData(
        String title,
        String description,
        String venueName,
        String venueAddress,
        Instant startsAt,
        Instant endsAt,
        int capacity) {

    Venue venue() {
        return new Venue(venueName, venueAddress);
    }
}
