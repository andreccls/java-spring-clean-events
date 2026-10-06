package com.andrecoura.events.application.port;

import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.domain.registration.Registration;
import java.util.Optional;
import java.util.UUID;

public interface RegistrationRepository {

    Registration save(Registration registration);

    Optional<Registration> findById(UUID id);

    long countActiveByEvent(UUID eventId);

    boolean existsActive(UUID eventId, UUID participantId);

    boolean existsByParticipant(UUID participantId);

    Page<Registration> findByEvent(UUID eventId, PageRequest pageRequest);
}
