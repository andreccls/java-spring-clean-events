package com.andrecoura.events.infrastructure.persistence;

import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.port.ParticipantRepository;
import com.andrecoura.events.domain.participant.Email;
import com.andrecoura.events.domain.participant.Participant;
import com.andrecoura.events.domain.shared.ConflictException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
class JpaParticipantRepository implements ParticipantRepository {

    private final ParticipantJpaRepository jpa;

    JpaParticipantRepository(ParticipantJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Participant save(Participant participant) {
        try {
            return jpa.saveAndFlush(ParticipantEntity.from(participant)).toDomain();
        } catch (DataIntegrityViolationException e) {
            // Lost a race against another request using the same e-mail (unique index uq_participants_email).
            throw new ConflictException("a participant with this email already exists");
        }
    }

    @Override
    public Optional<Participant> findById(UUID id) {
        return jpa.findById(id).map(ParticipantEntity::toDomain);
    }

    @Override
    public Optional<Participant> findByEmail(Email email) {
        return jpa.findByEmail(email.value()).map(ParticipantEntity::toDomain);
    }

    @Override
    public Page<Participant> findAll(PageRequest pageRequest) {
        var result = jpa.findAll(Pages.toSpring(pageRequest, Sort.by("name", "id")));
        return Pages.toApplication(result, pageRequest, ParticipantEntity::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
