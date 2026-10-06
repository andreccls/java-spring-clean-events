package com.andrecoura.events.infrastructure.persistence;

import com.andrecoura.events.domain.registration.RegistrationStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface RegistrationJpaRepository extends JpaRepository<RegistrationEntity, UUID> {

    long countByEventIdAndStatus(UUID eventId, RegistrationStatus status);

    boolean existsByEventIdAndParticipantIdAndStatus(UUID eventId, UUID participantId, RegistrationStatus status);

    boolean existsByParticipantId(UUID participantId);

    Page<RegistrationEntity> findByEventId(UUID eventId, Pageable pageable);
}
