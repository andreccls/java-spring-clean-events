package com.andrecoura.events.application.participant;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.port.ParticipantRepository;
import com.andrecoura.events.domain.shared.NotFoundException;
import java.util.UUID;

@UseCase
public class GetParticipant {

    private final ParticipantRepository participants;

    public GetParticipant(ParticipantRepository participants) {
        this.participants = participants;
    }

    public ParticipantView execute(UUID id) {
        return participants.findById(id).map(ParticipantView::from).orElseThrow(() -> new NotFoundException("Participant", id));
    }
}
