package com.andrecoura.events.domain.registration;

import com.andrecoura.events.domain.shared.ConflictException;
import java.time.Instant;
import java.util.UUID;

/** Aggregate root: a participant's seat in an event. Cancelling keeps the record (history) and frees the seat. */
public final class Registration {

    private final UUID id;
    private final UUID eventId;
    private final UUID participantId;
    private RegistrationStatus status;
    private final Instant registeredAt;
    private Instant cancelledAt;

    private Registration(
            UUID id,
            UUID eventId,
            UUID participantId,
            RegistrationStatus status,
            Instant registeredAt,
            Instant cancelledAt) {
        this.id = id;
        this.eventId = eventId;
        this.participantId = participantId;
        this.status = status;
        this.registeredAt = registeredAt;
        this.cancelledAt = cancelledAt;
    }

    public static Registration register(UUID eventId, UUID participantId, Instant now) {
        return new Registration(UUID.randomUUID(), eventId, participantId, RegistrationStatus.ACTIVE, now, null);
    }

    /** Rebuilds a persisted registration as-is (for persistence adapters). */
    public static Registration restore(
            UUID id,
            UUID eventId,
            UUID participantId,
            RegistrationStatus status,
            Instant registeredAt,
            Instant cancelledAt) {
        return new Registration(id, eventId, participantId, status, registeredAt, cancelledAt);
    }

    public void cancel(Instant now) {
        if (status == RegistrationStatus.CANCELLED) {
            throw new ConflictException("registration is already cancelled");
        }
        status = RegistrationStatus.CANCELLED;
        cancelledAt = now;
    }

    public UUID id() { return id; }
    public UUID eventId() { return eventId; }
    public UUID participantId() { return participantId; }
    public RegistrationStatus status() { return status; }
    public Instant registeredAt() { return registeredAt; }
    public Instant cancelledAt() { return cancelledAt; }
}
