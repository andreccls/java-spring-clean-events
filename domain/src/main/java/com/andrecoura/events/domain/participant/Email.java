package com.andrecoura.events.domain.participant;

import com.andrecoura.events.domain.shared.Rules;
import java.util.Locale;
import java.util.regex.Pattern;

/** Value object: a normalized (trimmed, lower-case) e-mail address. Deliberately simple validation. */
public record Email(String value) {

    private static final Pattern SHAPE = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        value = Rules.requiredText(value, 254, "email").toLowerCase(Locale.ROOT);
        Rules.require(SHAPE.matcher(value).matches(), "email is not valid");
    }
}
