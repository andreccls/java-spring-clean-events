package com.andrecoura.events.application.event;

import com.andrecoura.events.domain.event.Event;
import com.andrecoura.events.domain.event.EventStatus;
import java.time.Instant;
import java.util.UUID;

public record EventView(
        UUID id,
        String title,
        String description,
        VenueView venue,
        Instant startsAt,
        Instant endsAt,
        int capacity,
        EventStatus status) {

    public record VenueView(String name, String address) {}

    public static EventView from(Event e) {
        return new EventView(
                e.id(),
                e.title(),
                e.description(),
                new VenueView(e.venue().name(), e.venue().address()),
                e.startsAt(),
                e.endsAt(),
                e.capacity(),
                e.status());
    }
}
