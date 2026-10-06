package com.andrecoura.events.application.participant;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.port.ParticipantRepository;

@UseCase
public class ListParticipants {

    private final ParticipantRepository participants;

    public ListParticipants(ParticipantRepository participants) {
        this.participants = participants;
    }

    public Page<ParticipantView> execute(PageRequest pageRequest) {
        return participants.findAll(pageRequest).map(ParticipantView::from);
    }
}
