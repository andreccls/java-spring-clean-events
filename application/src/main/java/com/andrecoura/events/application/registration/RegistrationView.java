package com.andrecoura.events.application.registration;

import com.andrecoura.events.domain.registration.Registration;
import com.andrecoura.events.domain.registration.RegistrationStatus;
import java.time.Instant;
import java.util.UUID;

public record RegistrationView(
        UUID id,
        UUID eventId,
        UUID participantId,
        RegistrationStatus status,
        Instant registeredAt,
        Instant cancelledAt) {

    public static RegistrationView from(Registration r) {
        return new RegistrationView(r.id(), r.eventId(), r.participantId(), r.status(), r.registeredAt(), r.cancelledAt());
    }
}
