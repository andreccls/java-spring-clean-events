package com.andrecoura.events.application.event;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.port.EventRepository;
import com.andrecoura.events.domain.shared.NotFoundException;
import java.util.UUID;

@UseCase
public class GetEvent {

    private final EventRepository events;

    public GetEvent(EventRepository events) {
        this.events = events;
    }

    public EventView execute(UUID id) {
        return events.findById(id).map(EventView::from).orElseThrow(() -> new NotFoundException("Event", id));
    }
}
