package com.andrecoura.events.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ParticipantJpaRepository extends JpaRepository<ParticipantEntity, UUID> {

    Optional<ParticipantEntity> findByEmail(String email);
}
