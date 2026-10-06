package com.andrecoura.events.application.participant;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.port.ParticipantRepository;
import com.andrecoura.events.domain.participant.Email;
import com.andrecoura.events.domain.participant.Participant;
import com.andrecoura.events.domain.shared.ConflictException;

@UseCase
public class CreateParticipant {

    private final ParticipantRepository participants;

    public CreateParticipant(ParticipantRepository participants) {
        this.participants = participants;
    }

    public ParticipantView execute(ParticipantData data) {
        Email email = new Email(data.email());
        if (participants.findByEmail(email).isPresent()) {
            throw new ConflictException("a participant with this email already exists");
        }
        return ParticipantView.from(participants.save(Participant.create(data.name(), email)));
    }
}
