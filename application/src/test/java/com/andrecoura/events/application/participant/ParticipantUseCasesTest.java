package com.andrecoura.events.application.participant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.fakes.Fixtures;
import com.andrecoura.events.application.fakes.InMemoryParticipantRepository;
import com.andrecoura.events.application.fakes.InMemoryRegistrationRepository;
import com.andrecoura.events.domain.registration.Registration;
import com.andrecoura.events.domain.shared.ConflictException;
import com.andrecoura.events.domain.shared.DomainException;
import com.andrecoura.events.domain.shared.NotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ParticipantUseCasesTest {

    final InMemoryParticipantRepository participants = new InMemoryParticipantRepository();
    final InMemoryRegistrationRepository registrations = new InMemoryRegistrationRepository();
    final CreateParticipant create = new CreateParticipant(participants);
    final GetParticipant get = new GetParticipant(participants);
    final ListParticipants list = new ListParticipants(participants);
    final UpdateParticipant update = new UpdateParticipant(participants);
    final DeleteParticipant delete = new DeleteParticipant(participants, registrations);

    @Test
    void createsAndReadsBack() {
        ParticipantView created = create.execute(new ParticipantData("Maria", "Maria@Example.com"));

        assertThat(created.email()).isEqualTo("maria@example.com");
        assertThat(get.execute(created.id())).isEqualTo(created);
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        create.execute(new ParticipantData("Maria", "maria@example.com"));

        assertThatThrownBy(() -> create.execute(new ParticipantData("Other", "MARIA@example.com")))
                .isInstanceOf(ConflictException.class);
        assertThat(participants.count()).isEqualTo(1);
    }

    @Test
    void rejectsInvalidEmail() {
        assertThatThrownBy(() -> create.execute(new ParticipantData("Maria", "nope"))).isInstanceOf(DomainException.class);
    }

    @Test
    void getUnknownIsNotFound() {
        assertThatThrownBy(() -> get.execute(UUID.randomUUID())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void listsWithPaging() {
        create.execute(new ParticipantData("A", "a@example.com"));
        create.execute(new ParticipantData("B", "b@example.com"));

        var page = list.execute(PageRequest.of(2, 1));

        assertThat(page.items()).extracting(ParticipantView::name).containsExactly("B");
        assertThat(page.total()).isEqualTo(2);
    }

    @Test
    void updatesKeepingOwnEmail() {
        ParticipantView created = create.execute(new ParticipantData("Maria", "maria@example.com"));

        ParticipantView updated = update.execute(created.id(), new ParticipantData("Maria S.", "maria@example.com"));

        assertThat(updated.name()).isEqualTo("Maria S.");
    }

    @Test
    void updateRefusesAnotherParticipantsEmail() {
        create.execute(new ParticipantData("A", "a@example.com"));
        ParticipantView b = create.execute(new ParticipantData("B", "b@example.com"));

        assertThatThrownBy(() -> update.execute(b.id(), new ParticipantData("B", "a@example.com")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateUnknownIsNotFound() {
        assertThatThrownBy(() -> update.execute(UUID.randomUUID(), new ParticipantData("B", "b@example.com")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deletesWhenNoRegistrationsExist() {
        ParticipantView created = create.execute(new ParticipantData("Maria", "maria@example.com"));

        delete.execute(created.id());

        assertThat(participants.count()).isZero();
    }

    @Test
    void deleteIsRefusedWhenThereAreRegistrations() {
        ParticipantView created = create.execute(new ParticipantData("Maria", "maria@example.com"));
        registrations.save(Registration.register(UUID.randomUUID(), created.id(), Fixtures.NOW));

        assertThatThrownBy(() -> delete.execute(created.id())).isInstanceOf(ConflictException.class);
    }

    @Test
    void deleteUnknownIsNotFound() {
        assertThatThrownBy(() -> delete.execute(UUID.randomUUID())).isInstanceOf(NotFoundException.class);
    }
}
