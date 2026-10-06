package com.andrecoura.events.domain.shared;

/** A business rule or invariant was violated by the input (maps to HTTP 400). */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
