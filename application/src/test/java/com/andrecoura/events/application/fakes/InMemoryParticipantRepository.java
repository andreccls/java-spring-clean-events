package com.andrecoura.events.application.fakes;

import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.port.ParticipantRepository;
import com.andrecoura.events.domain.participant.Email;
import com.andrecoura.events.domain.participant.Participant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryParticipantRepository implements ParticipantRepository {

    private final Map<UUID, Participant> store = new LinkedHashMap<>();

    @Override
    public Participant save(Participant participant) {
        store.put(participant.id(), participant);
        return participant;
    }

    @Override
    public Optional<Participant> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Participant> findByEmail(Email email) {
        return store.values().stream().filter(p -> p.email().equals(email)).findFirst();
    }

    @Override
    public Page<Participant> findAll(PageRequest pageRequest) {
        List<Participant> all = List.copyOf(store.values());
        int from = Math.min(pageRequest.offset(), all.size());
        int to = Math.min(from + pageRequest.size(), all.size());
        return new Page<>(all.subList(from, to), pageRequest.page(), pageRequest.size(), all.size());
    }

    @Override
    public void deleteById(UUID id) {
        store.remove(id);
    }

    public int count() {
        return store.size();
    }
}
