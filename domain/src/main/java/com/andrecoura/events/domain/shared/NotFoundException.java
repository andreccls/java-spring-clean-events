package com.andrecoura.events.domain.shared;

/** The referenced aggregate does not exist (maps to HTTP 404). */
public class NotFoundException extends DomainException {

    public NotFoundException(String what, Object id) {
        super(what + " " + id + " not found");
    }
}
