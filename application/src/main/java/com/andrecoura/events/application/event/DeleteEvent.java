package com.andrecoura.events.application.event;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.port.EventRepository;
import com.andrecoura.events.domain.event.Event;
import com.andrecoura.events.domain.event.EventStatus;
import com.andrecoura.events.domain.shared.ConflictException;
import com.andrecoura.events.domain.shared.NotFoundException;
import java.util.UUID;

/** Only drafts are deleted (they cannot have registrations); published events are cancelled instead. */
@UseCase
public class DeleteEvent {

    private final EventRepository events;

    public DeleteEvent(EventRepository events) {
        this.events = events;
    }

    public void execute(UUID id) {
        Event event = events.findById(id).orElseThrow(() -> new NotFoundException("Event", id));
        if (event.status() != EventStatus.DRAFT) {
            throw new ConflictException("only DRAFT events can be deleted; cancel it instead");
        }
        events.deleteById(id);
    }
}
