package com.andrecoura.events.domain.event;

import com.andrecoura.events.domain.shared.ConflictException;
import com.andrecoura.events.domain.shared.Rules;
import java.time.Instant;
import java.util.UUID;

/** Aggregate root. All state changes go through methods that protect the invariants. */
public final class Event {

    private final UUID id;
    private String title;
    private String description;
    private Venue venue;
    private Instant startsAt;
    private Instant endsAt;
    private int capacity;
    private EventStatus status;

    private Event(UUID id, EventStatus status) {
        this.id = id;
        this.status = status;
    }

    public static Event create(
            String title, String description, Venue venue, Instant startsAt, Instant endsAt, int capacity) {
        Event event = new Event(UUID.randomUUID(), EventStatus.DRAFT);
        event.apply(title, description, venue, startsAt, endsAt, capacity);
        return event;
    }

    /** Rebuilds a persisted event as-is (for persistence adapters); it does not re-run creation rules. */
    public static Event restore(
            UUID id,
            String title,
            String description,
            Venue venue,
            Instant startsAt,
            Instant endsAt,
            int capacity,
            EventStatus status) {
        Event event = new Event(id, status);
        event.title = title;
        event.description = description;
        event.venue = venue;
        event.startsAt = startsAt;
        event.endsAt = endsAt;
        event.capacity = capacity;
        return event;
    }

    public void update(
            String title, String description, Venue venue, Instant startsAt, Instant endsAt, int capacity) {
        if (status == EventStatus.CANCELLED || status == EventStatus.FINISHED) {
            throw new ConflictException("a " + status + " event cannot be changed");
        }
        apply(title, description, venue, startsAt, endsAt, capacity);
    }

    public void publish(Instant now) {
        if (status != EventStatus.DRAFT) {
            throw new ConflictException("only a DRAFT event can be published");
        }
        Rules.require(capacity > 0, "capacity must be greater than zero to publish");
        Rules.require(startsAt.isAfter(now), "start must be in the future to publish");
        status = EventStatus.PUBLISHED;
    }

    public void cancel() {
        if (status == EventStatus.CANCELLED || status == EventStatus.FINISHED) {
            throw new ConflictException("a " + status + " event cannot be cancelled");
        }
        status = EventStatus.CANCELLED;
    }

    public void finish() {
        if (status != EventStatus.PUBLISHED) {
            throw new ConflictException("only a PUBLISHED event can be finished");
        }
        status = EventStatus.FINISHED;
    }

    /** Registration rule: the event must be published and have a free seat. */
    public void ensureCanRegister(long activeRegistrations) {
        if (status != EventStatus.PUBLISHED) {
            throw new ConflictException("registrations are only accepted for published events");
        }
        if (activeRegistrations >= capacity) {
            throw new ConflictException("event is full");
        }
    }

    private void apply(
            String title, String description, Venue venue, Instant startsAt, Instant endsAt, int capacity) {
        this.title = Rules.requiredText(title, 200, "title");
        Rules.require(description == null || description.length() <= 2000, "description must have at most 2000 characters");
        Rules.require(venue != null, "venue is required");
        Rules.require(startsAt != null, "start is required");
        Rules.require(endsAt != null, "end is required");
        Rules.require(endsAt.isAfter(startsAt), "end must be after start");
        Rules.require(capacity >= 0, "capacity cannot be negative");
        this.description = description;
        this.venue = venue;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.capacity = capacity;
    }

    public UUID id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public Venue venue() { return venue; }
    public Instant startsAt() { return startsAt; }
    public Instant endsAt() { return endsAt; }
    public int capacity() { return capacity; }
    public EventStatus status() { return status; }
}
