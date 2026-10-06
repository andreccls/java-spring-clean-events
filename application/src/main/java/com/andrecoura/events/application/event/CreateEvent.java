package com.andrecoura.events.application.event;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.port.EventRepository;
import com.andrecoura.events.domain.event.Event;

@UseCase
public class CreateEvent {

    private final EventRepository events;

    public CreateEvent(EventRepository events) {
        this.events = events;
    }

    public EventView execute(EventData data) {
        Event event = Event.create(
                data.title(), data.description(), data.venue(), data.startsAt(), data.endsAt(), data.capacity());
        return EventView.from(events.save(event));
    }
}
