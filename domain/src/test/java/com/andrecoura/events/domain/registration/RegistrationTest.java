package com.andrecoura.events.domain.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andrecoura.events.domain.shared.ConflictException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegistrationTest {

    static final Instant T0 = Instant.parse("2030-01-01T10:00:00Z");

    @Test
    void startsActive() {
        UUID event = UUID.randomUUID();
        UUID participant = UUID.randomUUID();

        Registration r = Registration.register(event, participant, T0);

        assertThat(r.id()).isNotNull();
        assertThat(r.eventId()).isEqualTo(event);
        assertThat(r.participantId()).isEqualTo(participant);
        assertThat(r.status()).isEqualTo(RegistrationStatus.ACTIVE);
        assertThat(r.registeredAt()).isEqualTo(T0);
        assertThat(r.cancelledAt()).isNull();
    }

    @Test
    void cancelKeepsHistory() {
        Registration r = Registration.register(UUID.randomUUID(), UUID.randomUUID(), T0);

        r.cancel(T0.plusSeconds(5));

        assertThat(r.status()).isEqualTo(RegistrationStatus.CANCELLED);
        assertThat(r.cancelledAt()).isEqualTo(T0.plusSeconds(5));
    }

    @Test
    void cannotCancelTwice() {
        Registration r = Registration.register(UUID.randomUUID(), UUID.randomUUID(), T0);
        r.cancel(T0);

        assertThatThrownBy(() -> r.cancel(T0)).isInstanceOf(ConflictException.class);
    }

    @Test
    void restoreRebuildsAsIs() {
        UUID id = UUID.randomUUID();
        UUID e = UUID.randomUUID();
        UUID p = UUID.randomUUID();

        Registration r = Registration.restore(id, e, p, RegistrationStatus.CANCELLED, T0, T0.plusSeconds(1));

        assertThat(r.id()).isEqualTo(id);
        assertThat(r.status()).isEqualTo(RegistrationStatus.CANCELLED);
        assertThat(r.cancelledAt()).isEqualTo(T0.plusSeconds(1));
    }
}
