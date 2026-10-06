package com.andrecoura.events.application.event;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.port.EventRepository;
import com.andrecoura.events.domain.event.Event;
import com.andrecoura.events.domain.shared.NotFoundException;
import java.time.Clock;
import java.util.UUID;

@UseCase
public class PublishEvent {

    private final EventRepository events;
    private final Clock clock;

    public PublishEvent(EventRepository events, Clock clock) {
        this.events = events;
        this.clock = clock;
    }

    public EventView execute(UUID id) {
        Event event = events.findById(id).orElseThrow(() -> new NotFoundException("Event", id));
        event.publish(clock.instant());
        return EventView.from(events.save(event));
    }
}
