package com.andrecoura.events.domain.participant;

import com.andrecoura.events.domain.shared.Rules;
import java.util.UUID;

/** Aggregate root: a person who can register for events. The e-mail identifies the person. */
public final class Participant {

    private final UUID id;
    private String name;
    private Email email;

    private Participant(UUID id) {
        this.id = id;
    }

    public static Participant create(String name, Email email) {
        Participant participant = new Participant(UUID.randomUUID());
        participant.update(name, email);
        return participant;
    }

    /** Rebuilds a persisted participant as-is (for persistence adapters). */
    public static Participant restore(UUID id, String name, Email email) {
        Participant participant = new Participant(id);
        participant.name = name;
        participant.email = email;
        return participant;
    }

    public void update(String name, Email email) {
        this.name = Rules.requiredText(name, 120, "name");
        Rules.require(email != null, "email is required");
        this.email = email;
    }

    public UUID id() { return id; }
    public String name() { return name; }
    public Email email() { return email; }
}
