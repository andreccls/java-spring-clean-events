package com.andrecoura.events.application.port;

import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.domain.participant.Email;
import com.andrecoura.events.domain.participant.Participant;
import java.util.Optional;
import java.util.UUID;

public interface ParticipantRepository {

    Participant save(Participant participant);

    Optional<Participant> findById(UUID id);

    Optional<Participant> findByEmail(Email email);

    Page<Participant> findAll(PageRequest pageRequest);

    void deleteById(UUID id);
}
