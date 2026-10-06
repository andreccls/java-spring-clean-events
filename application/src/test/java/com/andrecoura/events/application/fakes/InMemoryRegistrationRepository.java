package com.andrecoura.events.application.fakes;

import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.port.RegistrationRepository;
import com.andrecoura.events.domain.registration.Registration;
import com.andrecoura.events.domain.registration.RegistrationStatus;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryRegistrationRepository implements RegistrationRepository {

    private final Map<UUID, Registration> store = new LinkedHashMap<>();

    @Override
    public Registration save(Registration registration) {
        store.put(registration.id(), registration);
        return registration;
    }

    @Override
    public Optional<Registration> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public long countActiveByEvent(UUID eventId) {
        return store.values().stream()
                .filter(r -> r.eventId().equals(eventId) && r.status() == RegistrationStatus.ACTIVE)
                .count();
    }

    @Override
    public boolean existsActive(UUID eventId, UUID participantId) {
        return store.values().stream()
                .anyMatch(r -> r.eventId().equals(eventId)
                        && r.participantId().equals(participantId)
                        && r.status() == RegistrationStatus.ACTIVE);
    }

    @Override
    public boolean existsByParticipant(UUID participantId) {
        return store.values().stream().anyMatch(r -> r.participantId().equals(participantId));
    }

    @Override
    public Page<Registration> findByEvent(UUID eventId, PageRequest pageRequest) {
        List<Registration> all = store.values().stream().filter(r -> r.eventId().equals(eventId)).toList();
        int from = Math.min(pageRequest.offset(), all.size());
        int to = Math.min(from + pageRequest.size(), all.size());
        return new Page<>(all.subList(from, to), pageRequest.page(), pageRequest.size(), all.size());
    }
}
