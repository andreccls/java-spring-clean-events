package com.andrecoura.events.application.participant;

/** Editable fields of a participant, shared by the create and update commands. */
public record ParticipantData(String name, String email) {}
