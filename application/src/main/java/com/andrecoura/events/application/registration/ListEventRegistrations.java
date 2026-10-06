package com.andrecoura.events.application.registration;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.port.EventRepository;
import com.andrecoura.events.application.port.RegistrationRepository;
import com.andrecoura.events.domain.shared.NotFoundException;
import java.util.UUID;

@UseCase
public class ListEventRegistrations {

    private final EventRepository events;
    private final RegistrationRepository registrations;

    public ListEventRegistrations(EventRepository events, RegistrationRepository registrations) {
        this.events = events;
        this.registrations = registrations;
    }

    public Page<RegistrationView> execute(UUID eventId, PageRequest pageRequest) {
        events.findById(eventId).orElseThrow(() -> new NotFoundException("Event", eventId));
        return registrations.findByEvent(eventId, pageRequest).map(RegistrationView::from);
    }
}
