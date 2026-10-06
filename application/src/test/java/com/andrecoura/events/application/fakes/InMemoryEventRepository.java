package com.andrecoura.events.application.fakes;

import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.event.EventFilter;
import com.andrecoura.events.application.port.EventRepository;
import com.andrecoura.events.domain.event.Event;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Test double: same contract as the JPA adapter, kept in a map. */
public class InMemoryEventRepository implements EventRepository {

    private final Map<UUID, Event> store = new LinkedHashMap<>();

    @Override
    public Event save(Event event) {
        store.put(event.id(), event);
        return event;
    }

    @Override
    public Optional<Event> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Event> findByIdForUpdate(UUID id) {
        return findById(id);
    }

    @Override
    public Page<Event> search(EventFilter filter, PageRequest pageRequest) {
        List<Event> matches = store.values().stream()
                .filter(e -> filter.status() == null || e.status() == filter.status())
                .filter(e -> filter.from() == null || !e.startsAt().isBefore(filter.from()))
                .filter(e -> filter.to() == null || e.startsAt().isBefore(filter.to()))
                .sorted(Comparator.comparing(Event::startsAt))
                .toList();
        int from = Math.min(pageRequest.offset(), matches.size());
        int to = Math.min(from + pageRequest.size(), matches.size());
        return new Page<>(matches.subList(from, to), pageRequest.page(), pageRequest.size(), matches.size());
    }

    @Override
    public void deleteById(UUID id) {
        store.remove(id);
    }

    public int count() {
        return store.size();
    }
}
