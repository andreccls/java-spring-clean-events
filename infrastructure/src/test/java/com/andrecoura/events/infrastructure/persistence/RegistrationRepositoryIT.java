package com.andrecoura.events.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.port.EventRepository;
import com.andrecoura.events.application.port.ParticipantRepository;
import com.andrecoura.events.application.port.RegistrationRepository;
import com.andrecoura.events.domain.event.Event;
import com.andrecoura.events.domain.event.Venue;
import com.andrecoura.events.domain.participant.Email;
import com.andrecoura.events.domain.participant.Participant;
import com.andrecoura.events.domain.registration.Registration;
import com.andrecoura.events.domain.registration.RegistrationStatus;
import com.andrecoura.events.domain.shared.ConflictException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RegistrationRepositoryIT extends PersistenceIT {

    static final Instant T0 = Instant.parse("2031-03-01T12:00:00.5Z");

    @Autowired EventRepository events;
    @Autowired ParticipantRepository participants;
    @Autowired RegistrationRepository registrations;

    UUID eventId;
    UUID participantId;

    @BeforeEach
    void seed() {
        eventId = events.save(Event.create("E", null, new Venue("v", "a"), T0, T0.plusSeconds(60), 5)).id();
        participantId = participants.save(Participant.create("Maria", new Email("maria@example.com"))).id();
    }

    @Test
    void roundTripsAndCounts() {
        Registration saved = registrations.save(Registration.register(eventId, participantId, T0));

        assertThat(registrations.findById(saved.id())).get().usingRecursiveComparison().isEqualTo(saved);
        assertThat(registrations.countActiveByEvent(eventId)).isEqualTo(1);
        assertThat(registrations.existsActive(eventId, participantId)).isTrue();
        assertThat(registrations.existsByParticipant(participantId)).isTrue();
        assertThat(registrations.existsByParticipant(UUID.randomUUID())).isFalse();
    }

    @Test
    void cancellingFreesTheActiveSlotButKeepsHistory() {
        Registration saved = registrations.save(Registration.register(eventId, participantId, T0));
        saved.cancel(T0.plusSeconds(1));
        registrations.save(saved);

        assertThat(registrations.countActiveByEvent(eventId)).isZero();
        assertThat(registrations.existsActive(eventId, participantId)).isFalse();
        assertThat(registrations.findById(saved.id())).get()
                .extracting(Registration::status, Registration::cancelledAt)
                .containsExactly(RegistrationStatus.CANCELLED, T0.plusSeconds(1));

        // the generated active_key column is NULL for cancelled rows, so a new active one is allowed
        registrations.save(Registration.register(eventId, participantId, T0.plusSeconds(2)));
        assertThat(registrations.countActiveByEvent(eventId)).isEqualTo(1);
        assertThat(registrations.findByEvent(eventId, PageRequest.of(null, null)).total()).isEqualTo(2);
    }

    @Test
    void uniqueIndexRejectsASecondActiveRegistration() {
        registrations.save(Registration.register(eventId, participantId, T0));

        assertThatThrownBy(() -> registrations.save(Registration.register(eventId, participantId, T0)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void listsByEventPaginated() {
        for (int i = 0; i < 3; i++) {
            UUID p = participants.save(Participant.create("P" + i, new Email("p" + i + "@example.com"))).id();
            registrations.save(Registration.register(eventId, p, T0.plusSeconds(i)));
        }

        var page = registrations.findByEvent(eventId, PageRequest.of(2, 2));

        assertThat(page.items()).hasSize(1);
        assertThat(page.total()).isEqualTo(3);
    }
}
