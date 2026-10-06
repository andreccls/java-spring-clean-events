package com.andrecoura.events.domain.participant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andrecoura.events.domain.shared.DomainException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ParticipantTest {

    @Test
    void normalizesEmailAndTrimsName() {
        Participant p = Participant.create("  Maria Silva ", new Email("  Maria@Example.COM "));

        assertThat(p.id()).isNotNull();
        assertThat(p.name()).isEqualTo("Maria Silva");
        assertThat(p.email().value()).isEqualTo("maria@example.com");
    }

    @Test
    void rejectsInvalidName() {
        Email email = new Email("a@b.co");

        assertThatThrownBy(() -> Participant.create(null, email)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> Participant.create(" ", email)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> Participant.create("x".repeat(121), email)).isInstanceOf(DomainException.class);
    }

    @Test
    void requiresEmail() {
        assertThatThrownBy(() -> Participant.create("Ana", null)).isInstanceOf(DomainException.class);
    }

    @Test
    void updateChangesNameAndEmailKeepingInvariants() {
        Participant p = Participant.create("Ana", new Email("ana@example.com"));

        p.update("Ana Souza", new Email("ana.souza@example.com"));

        assertThat(p.name()).isEqualTo("Ana Souza");
        assertThat(p.email().value()).isEqualTo("ana.souza@example.com");
        assertThatThrownBy(() -> p.update(" ", new Email("x@y.zz"))).isInstanceOf(DomainException.class);
    }

    @Test
    void restoreKeepsId() {
        UUID id = UUID.randomUUID();

        assertThat(Participant.restore(id, "Ana", new Email("ana@example.com")).id()).isEqualTo(id);
    }
}
