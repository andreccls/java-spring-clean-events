package com.andrecoura.events.domain.event;

/** DRAFT -> PUBLISHED -> FINISHED, and DRAFT/PUBLISHED -> CANCELLED. */
public enum EventStatus {
    DRAFT,
    PUBLISHED,
    CANCELLED,
    FINISHED
}
