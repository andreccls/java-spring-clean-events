package com.andrecoura.events.infrastructure.persistence;

import com.andrecoura.events.domain.participant.Email;
import com.andrecoura.events.domain.participant.Participant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "participants")
class ParticipantEntity {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "char(36)")
    UUID id;

    String name;
    String email;

    protected ParticipantEntity() {}

    static ParticipantEntity from(Participant p) {
        ParticipantEntity entity = new ParticipantEntity();
        entity.id = p.id();
        entity.name = p.name();
        entity.email = p.email().value();
        return entity;
    }

    Participant toDomain() {
        return Participant.restore(id, name, new Email(email));
    }
}
