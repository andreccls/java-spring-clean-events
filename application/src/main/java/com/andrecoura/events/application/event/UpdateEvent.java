package com.andrecoura.events.application.event;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.port.EventRepository;
import com.andrecoura.events.application.port.RegistrationRepository;
import com.andrecoura.events.application.port.Transaction;
import com.andrecoura.events.domain.event.Event;
import com.andrecoura.events.domain.shared.ConflictException;
import com.andrecoura.events.domain.shared.NotFoundException;
import java.util.UUID;

@UseCase
public class UpdateEvent {

    private final EventRepository events;
    private final RegistrationRepository registrations;
    private final Transaction transaction;

    public UpdateEvent(EventRepository events, RegistrationRepository registrations, Transaction transaction) {
        this.events = events;
        this.registrations = registrations;
        this.transaction = transaction;
    }

    public EventView execute(UUID id, EventData data) {
        return transaction.execute(() -> {
            Event event = events.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Event", id));
            if (data.capacity() < registrations.countActiveByEvent(id)) {
                throw new ConflictException("capacity cannot be lower than the number of active registrations");
            }
            event.update(
                    data.title(), data.description(), data.venue(), data.startsAt(), data.endsAt(), data.capacity());
            return EventView.from(events.save(event));
        });
    }
}
