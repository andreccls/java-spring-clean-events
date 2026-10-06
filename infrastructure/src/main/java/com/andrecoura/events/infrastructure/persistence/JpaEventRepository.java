package com.andrecoura.events.infrastructure.persistence;

import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.event.EventFilter;
import com.andrecoura.events.application.port.EventRepository;
import com.andrecoura.events.domain.event.Event;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
class JpaEventRepository implements EventRepository {

    private final EventJpaRepository jpa;

    JpaEventRepository(EventJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Event save(Event event) {
        return jpa.save(EventEntity.from(event)).toDomain();
    }

    @Override
    public Optional<Event> findById(UUID id) {
        return jpa.findById(id).map(EventEntity::toDomain);
    }

    @Override
    public Optional<Event> findByIdForUpdate(UUID id) {
        return jpa.findByIdForUpdate(id).map(EventEntity::toDomain);
    }

    @Override
    public Page<Event> search(EventFilter filter, PageRequest pageRequest) {
        // The ORDER BY is in the query itself, so the Pageable carries no sort.
        var result = jpa.search(filter.status(), filter.from(), filter.to(), Pages.toSpring(pageRequest, Sort.unsorted()));
        return Pages.toApplication(result, pageRequest, EventEntity::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
