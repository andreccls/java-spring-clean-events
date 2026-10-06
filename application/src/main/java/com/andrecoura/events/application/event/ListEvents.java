package com.andrecoura.events.application.event;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.port.EventRepository;

@UseCase
public class ListEvents {

    private final EventRepository events;

    public ListEvents(EventRepository events) {
        this.events = events;
    }

    public Page<EventView> execute(EventFilter filter, PageRequest pageRequest) {
        return events.search(filter, pageRequest).map(EventView::from);
    }
}
