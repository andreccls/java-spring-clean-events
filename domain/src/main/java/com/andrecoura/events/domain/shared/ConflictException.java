package com.andrecoura.events.domain.shared;

/** The request is valid but clashes with the current state (maps to HTTP 409). */
public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(message);
    }
}
