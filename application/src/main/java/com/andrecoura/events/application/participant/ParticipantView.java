package com.andrecoura.events.application.participant;

import com.andrecoura.events.domain.participant.Participant;
import java.util.UUID;

public record ParticipantView(UUID id, String name, String email) {

    public static ParticipantView from(Participant p) {
        return new ParticipantView(p.id(), p.name(), p.email().value());
    }
}
