package com.andrecoura.events.application.participant;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.port.ParticipantRepository;
import com.andrecoura.events.application.port.RegistrationRepository;
import com.andrecoura.events.domain.shared.ConflictException;
import com.andrecoura.events.domain.shared.NotFoundException;
import java.util.UUID;

@UseCase
public class DeleteParticipant {

    private final ParticipantRepository participants;
    private final RegistrationRepository registrations;

    public DeleteParticipant(ParticipantRepository participants, RegistrationRepository registrations) {
        this.participants = participants;
        this.registrations = registrations;
    }

    public void execute(UUID id) {
        participants.findById(id).orElseThrow(() -> new NotFoundException("Participant", id));
        if (registrations.existsByParticipant(id)) {
            throw new ConflictException("participant has registrations and cannot be deleted");
        }
        participants.deleteById(id);
    }
}
