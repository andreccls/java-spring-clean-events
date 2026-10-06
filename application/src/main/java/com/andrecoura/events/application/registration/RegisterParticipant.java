package com.andrecoura.events.application.registration;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.port.EventRepository;
import com.andrecoura.events.application.port.ParticipantRepository;
import com.andrecoura.events.application.port.RegistrationRepository;
import com.andrecoura.events.application.port.Transaction;
import com.andrecoura.events.domain.event.Event;
import com.andrecoura.events.domain.registration.Registration;
import com.andrecoura.events.domain.shared.ConflictException;
import com.andrecoura.events.domain.shared.NotFoundException;
import java.time.Clock;
import java.util.UUID;

/**
 * Registers a participant in an event. The event row is locked for the duration of the transaction,
 * so the capacity check and the insert cannot race with another registration.
 */
@UseCase
public class RegisterParticipant {

    private final EventRepository events;
    private final ParticipantRepository participants;
    private final RegistrationRepository registrations;
    private final Transaction transaction;
    private final Clock clock;

    public RegisterParticipant(
            EventRepository events,
            ParticipantRepository participants,
            RegistrationRepository registrations,
            Transaction transaction,
            Clock clock) {
        this.events = events;
        this.participants = participants;
        this.registrations = registrations;
        this.transaction = transaction;
        this.clock = clock;
    }

    public RegistrationView execute(UUID eventId, UUID participantId) {
        return transaction.execute(() -> {
            Event event = events.findByIdForUpdate(eventId).orElseThrow(() -> new NotFoundException("Event", eventId));
            participants.findById(participantId).orElseThrow(() -> new NotFoundException("Participant", participantId));
            if (registrations.existsActive(eventId, participantId)) {
                throw new ConflictException("participant is already registered in this event");
            }
            event.ensureCanRegister(registrations.countActiveByEvent(eventId));
            return RegistrationView.from(registrations.save(Registration.register(eventId, participantId, clock.instant())));
        });
    }
}
