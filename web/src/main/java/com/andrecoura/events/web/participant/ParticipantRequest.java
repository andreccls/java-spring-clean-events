package com.andrecoura.events.web.participant;

import com.andrecoura.events.application.participant.ParticipantData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record ParticipantRequest(@NotBlank @Size(max = 120) String name, @NotBlank @Size(max = 254) String email) {

    ParticipantData toData() {
        return new ParticipantData(name, email);
    }
}
