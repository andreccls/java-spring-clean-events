package com.andrecoura.events.application.registration;

import static com.andrecoura.events.application.fakes.Fixtures.CLOCK;
import static com.andrecoura.events.application.fakes.Fixtures.DIRECT;
import static com.andrecoura.events.application.fakes.Fixtures.NOW;
import static com.andrecoura.events.application.fakes.Fixtures.eventData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.event.CreateEvent;
import com.andrecoura.events.application.event.PublishEvent;
import com.andrecoura.events.application.fakes.InMemoryEventRepository;
import com.andrecoura.events.application.fakes.InMemoryParticipantRepository;
import com.andrecoura.events.application.fakes.InMemoryRegistrationRepository;
import com.andrecoura.events.application.participant.CreateParticipant;
import com.andrecoura.events.application.participant.ParticipantData;
import com.andrecoura.events.domain.registration.RegistrationStatus;
import com.andrecoura.events.domain.shared.ConflictException;
import com.andrecoura.events.domain.shared.NotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegistrationUseCasesTest {

    final InMemoryEventRepository events = new InMemoryEventRepository();
    final InMemoryParticipantRepository participants = new InMemoryParticipantRepository();
    final InMemoryRegistrationRepository registrations = new InMemoryRegistrationRepository();
    final RegisterParticipant register = new RegisterParticipant(events, participants, registrations, DIRECT, CLOCK);
    final CancelRegistration cancel = new CancelRegistration(registrations, CLOCK);
    final ListEventRegistrations list = new ListEventRegistrations(events, registrations);

    UUID publishedEvent(int capacity) {
        UUID id = new CreateEvent(events).execute(eventData(capacity)).id();
        new PublishEvent(events, CLOCK).execute(id);
        return id;
    }

    UUID participant(String email) {
        return new CreateParticipant(participants).execute(new ParticipantData("Someone", email)).id();
    }

    @Test
    void registersAnActiveSeat() {
        UUID event = publishedEvent(2);
        UUID p = participant("a@example.com");

        RegistrationView view = register.execute(event, p);

        assertThat(view.status()).isEqualTo(RegistrationStatus.ACTIVE);
        assertThat(view.registeredAt()).isEqualTo(NOW);
        assertThat(registrations.countActiveByEvent(event)).isEqualTo(1);
    }

    @Test
    void unknownEventOrParticipantIsNotFound() {
        UUID event = publishedEvent(2);
        UUID p = participant("a@example.com");

        assertThatThrownBy(() -> register.execute(UUID.randomUUID(), p)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> register.execute(event, UUID.randomUUID())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void refusesDuplicateActiveRegistration() {
        UUID event = publishedEvent(5);
        UUID p = participant("a@example.com");
        register.execute(event, p);

        assertThatThrownBy(() -> register.execute(event, p)).isInstanceOf(ConflictException.class).hasMessageContaining("already");
    }

    @Test
    void refusesWhenFullAndAcceptsAgainAfterACancellation() {
        UUID event = publishedEvent(1);
        UUID first = participant("a@example.com");
        UUID second = participant("b@example.com");
        RegistrationView seat = register.execute(event, first);

        assertThatThrownBy(() -> register.execute(event, second)).isInstanceOf(ConflictException.class).hasMessageContaining("full");

        cancel.execute(seat.id());
        assertThat(register.execute(event, second).status()).isEqualTo(RegistrationStatus.ACTIVE);
    }

    @Test
    void cancelledParticipantCanRegisterAgain() {
        UUID event = publishedEvent(1);
        UUID p = participant("a@example.com");
        cancel.execute(register.execute(event, p).id());

        register.execute(event, p);

        assertThat(registrations.countActiveByEvent(event)).isEqualTo(1);
    }

    @Test
    void refusesWhenEventIsNotPublished() {
        UUID draft = new CreateEvent(events).execute(eventData(5)).id();
        UUID p = participant("a@example.com");

        assertThatThrownBy(() -> register.execute(draft, p)).isInstanceOf(ConflictException.class).hasMessageContaining("published");
    }

    @Test
    void cancelRecordsTimeAndFreesTheSeat() {
        UUID event = publishedEvent(1);
        RegistrationView seat = register.execute(event, participant("a@example.com"));

        RegistrationView cancelled = cancel.execute(seat.id());

        assertThat(cancelled.status()).isEqualTo(RegistrationStatus.CANCELLED);
        assertThat(cancelled.cancelledAt()).isEqualTo(NOW);
        assertThat(registrations.countActiveByEvent(event)).isZero();
    }

    @Test
    void cancelUnknownIsNotFoundAndCancelTwiceIsConflict() {
        UUID event = publishedEvent(1);
        RegistrationView seat = register.execute(event, participant("a@example.com"));
        cancel.execute(seat.id());

        assertThatThrownBy(() -> cancel.execute(UUID.randomUUID())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> cancel.execute(seat.id())).isInstanceOf(ConflictException.class);
    }

    @Test
    void listsAnEventsRegistrationsIncludingCancelled() {
        UUID event = publishedEvent(5);
        RegistrationView a = register.execute(event, participant("a@example.com"));
        register.execute(event, participant("b@example.com"));
        cancel.execute(a.id());

        var page = list.execute(event, PageRequest.of(null, null));

        assertThat(page.total()).isEqualTo(2);
        assertThat(page.items()).extracting(RegistrationView::status)
                .containsExactly(RegistrationStatus.CANCELLED, RegistrationStatus.ACTIVE);
    }

    @Test
    void listForUnknownEventIsNotFound() {
        assertThatThrownBy(() -> list.execute(UUID.randomUUID(), PageRequest.of(null, null))).isInstanceOf(NotFoundException.class);
    }
}
