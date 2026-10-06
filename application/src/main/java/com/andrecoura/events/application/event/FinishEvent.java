package com.andrecoura.events.application.event;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.port.EventRepository;
import com.andrecoura.events.domain.event.Event;
import com.andrecoura.events.domain.shared.NotFoundException;
import java.util.UUID;

@UseCase
public class FinishEvent {

    private final EventRepository events;

    public FinishEvent(EventRepository events) {
        this.events = events;
    }

    public EventView execute(UUID id) {
        Event event = events.findById(id).orElseThrow(() -> new NotFoundException("Event", id));
        event.finish();
        return EventView.from(events.save(event));
    }
}
