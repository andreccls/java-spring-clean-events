package com.andrecoura.events.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.port.ParticipantRepository;
import com.andrecoura.events.domain.participant.Email;
import com.andrecoura.events.domain.participant.Participant;
import com.andrecoura.events.domain.shared.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ParticipantRepositoryIT extends PersistenceIT {

    @Autowired
    ParticipantRepository participants;

    @Test
    void roundTripsAndFindsByEmail() {
        Participant saved = participants.save(Participant.create("Maria", new Email("maria@example.com")));

        assertThat(participants.findById(saved.id())).get().usingRecursiveComparison().isEqualTo(saved);
        assertThat(participants.findByEmail(new Email("MARIA@example.com"))).get().extracting(Participant::id).isEqualTo(saved.id());
        assertThat(participants.findByEmail(new Email("other@example.com"))).isEmpty();
    }

    @Test
    void uniqueIndexTurnsAnEmailRaceIntoAConflict() {
        participants.save(Participant.create("Maria", new Email("maria@example.com")));

        assertThatThrownBy(() -> participants.save(Participant.create("Impostor", new Email("maria@example.com"))))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void listsPaginatedByName() {
        participants.save(Participant.create("Carol", new Email("c@example.com")));
        participants.save(Participant.create("Alice", new Email("a@example.com")));
        participants.save(Participant.create("Bob", new Email("b@example.com")));

        var page = participants.findAll(PageRequest.of(1, 2));

        assertThat(page.items()).extracting(Participant::name).containsExactly("Alice", "Bob");
        assertThat(page.total()).isEqualTo(3);
    }

    @Test
    void deletes() {
        Participant saved = participants.save(Participant.create("Maria", new Email("maria@example.com")));

        participants.deleteById(saved.id());

        assertThat(participants.findById(saved.id())).isEmpty();
    }
}
