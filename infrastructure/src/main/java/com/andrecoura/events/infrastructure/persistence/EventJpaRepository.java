package com.andrecoura.events.infrastructure.persistence;

import com.andrecoura.events.domain.event.EventStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface EventJpaRepository extends JpaRepository<EventEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EventEntity e where e.id = :id")
    Optional<EventEntity> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
            select e from EventEntity e
            where (:status is null or e.status = :status)
              and (:from is null or e.startsAt >= :from)
              and (:to is null or e.startsAt < :to)
            order by e.startsAt asc, e.id asc
            """)
    Page<EventEntity> search(
            @Param("status") EventStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable);
}
