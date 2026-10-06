package com.andrecoura.events.application.port;

import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.event.EventFilter;
import com.andrecoura.events.domain.event.Event;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {

    Event save(Event event);

    Optional<Event> findById(UUID id);

    /** Like {@link #findById} but serializes concurrent callers on the same event (must run inside a {@link Transaction}). */
    Optional<Event> findByIdForUpdate(UUID id);

    Page<Event> search(EventFilter filter, PageRequest pageRequest);

    void deleteById(UUID id);
}
