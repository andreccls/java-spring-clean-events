package com.andrecoura.events.infrastructure.persistence;

import com.andrecoura.events.domain.registration.Registration;
import com.andrecoura.events.domain.registration.RegistrationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "registrations")
class RegistrationEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "char(36)")
    UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "event_id", columnDefinition = "char(36)")
    UUID eventId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "participant_id", columnDefinition = "char(36)")
    UUID participantId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    RegistrationStatus status;

    @Column(name = "registered_at")
    Instant registeredAt;

    @Column(name = "cancelled_at")
    Instant cancelledAt;

    protected RegistrationEntity() {}

    static RegistrationEntity from(Registration r) {
        RegistrationEntity entity = new RegistrationEntity();
        entity.id = r.id();
        entity.eventId = r.eventId();
        entity.participantId = r.participantId();
        entity.status = r.status();
        entity.registeredAt = r.registeredAt();
        entity.cancelledAt = r.cancelledAt();
        return entity;
    }

    Registration toDomain() {
        return Registration.restore(id, eventId, participantId, status, registeredAt, cancelledAt);
    }
}
