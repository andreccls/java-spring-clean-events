package com.andrecoura.events.infrastructure.persistence;

import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.port.RegistrationRepository;
import com.andrecoura.events.domain.registration.Registration;
import com.andrecoura.events.domain.registration.RegistrationStatus;
import com.andrecoura.events.domain.shared.ConflictException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
class JpaRegistrationRepository implements RegistrationRepository {

    private final RegistrationJpaRepository jpa;

    JpaRegistrationRepository(RegistrationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Registration save(Registration registration) {
        try {
            return jpa.saveAndFlush(RegistrationEntity.from(registration)).toDomain();
        } catch (DataIntegrityViolationException e) {
            // Safety net behind the event lock: uq_registrations_one_active rejected a second active seat.
            throw new ConflictException("participant is already registered in this event");
        }
    }

    @Override
    public Optional<Registration> findById(UUID id) {
        return jpa.findById(id).map(RegistrationEntity::toDomain);
    }

    @Override
    public long countActiveByEvent(UUID eventId) {
        return jpa.countByEventIdAndStatus(eventId, RegistrationStatus.ACTIVE);
    }

    @Override
    public boolean existsActive(UUID eventId, UUID participantId) {
        return jpa.existsByEventIdAndParticipantIdAndStatus(eventId, participantId, RegistrationStatus.ACTIVE);
    }

    @Override
    public boolean existsByParticipant(UUID participantId) {
        return jpa.existsByParticipantId(participantId);
    }

    @Override
    public Page<Registration> findByEvent(UUID eventId, PageRequest pageRequest) {
        var result = jpa.findByEventId(eventId, Pages.toSpring(pageRequest, Sort.by("registeredAt", "id")));
        return Pages.toApplication(result, pageRequest, RegistrationEntity::toDomain);
    }
}
