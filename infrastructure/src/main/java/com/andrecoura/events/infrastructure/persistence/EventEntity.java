package com.andrecoura.events.infrastructure.persistence;

import com.andrecoura.events.domain.event.Event;
import com.andrecoura.events.domain.event.EventStatus;
import com.andrecoura.events.domain.event.Venue;
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

/** JPA representation of {@link Event}. Mapping is manual and lives here (see ADR 0003). */
@Entity
@Table(name = "events")
class EventEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "char(36)")
    UUID id;

    String title;
    String description;

    @Column(name = "venue_name")
    String venueName;

    @Column(name = "venue_address")
    String venueAddress;

    @Column(name = "starts_at")
    Instant startsAt;

    @Column(name = "ends_at")
    Instant endsAt;

    int capacity;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    EventStatus status;

    protected EventEntity() {}

    static EventEntity from(Event e) {
        EventEntity entity = new EventEntity();
        entity.id = e.id();
        entity.title = e.title();
        entity.description = e.description();
        entity.venueName = e.venue().name();
        entity.venueAddress = e.venue().address();
        entity.startsAt = e.startsAt();
        entity.endsAt = e.endsAt();
        entity.capacity = e.capacity();
        entity.status = e.status();
        return entity;
    }

    Event toDomain() {
        return Event.restore(
                id, title, description, new Venue(venueName, venueAddress), startsAt, endsAt, capacity, status);
    }
}
