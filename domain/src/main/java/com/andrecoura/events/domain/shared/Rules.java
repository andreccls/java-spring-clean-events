package com.andrecoura.events.domain.shared;

/** Tiny guard helpers so invariants read as one line. */
public final class Rules {

    private Rules() {}

    public static void require(boolean condition, String message) {
        if (!condition) {
            throw new DomainException(message);
        }
    }

    /** Trims and validates a required text with a maximum length. */
    public static String requiredText(String value, int maxLength, String field) {
        require(value != null && !value.isBlank(), field + " is required");
        String trimmed = value.trim();
        require(trimmed.length() <= maxLength, field + " must have at most " + maxLength + " characters");
        return trimmed;
    }
}
