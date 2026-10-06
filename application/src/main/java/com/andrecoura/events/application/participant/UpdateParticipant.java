package com.andrecoura.events.application.participant;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.port.ParticipantRepository;
import com.andrecoura.events.domain.participant.Email;
import com.andrecoura.events.domain.participant.Participant;
import com.andrecoura.events.domain.shared.ConflictException;
import com.andrecoura.events.domain.shared.NotFoundException;
import java.util.UUID;

@UseCase
public class UpdateParticipant {

    private final ParticipantRepository participants;

    public UpdateParticipant(ParticipantRepository participants) {
        this.participants = participants;
    }

    public ParticipantView execute(UUID id, ParticipantData data) {
        Participant participant = participants.findById(id).orElseThrow(() -> new NotFoundException("Participant", id));
        Email email = new Email(data.email());
        boolean takenByAnother = participants.findByEmail(email).filter(other -> !other.id().equals(id)).isPresent();
        if (takenByAnother) {
            throw new ConflictException("a participant with this email already exists");
        }
        participant.update(data.name(), email);
        return ParticipantView.from(participants.save(participant));
    }
}
