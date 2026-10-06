package com.andrecoura.events.application.event;

import com.andrecoura.events.domain.event.EventStatus;
import java.time.Instant;

/** Optional criteria; {@code from} is inclusive and {@code to} exclusive, both applied to the start time. */
public record EventFilter(EventStatus status, Instant from, Instant to) {}
