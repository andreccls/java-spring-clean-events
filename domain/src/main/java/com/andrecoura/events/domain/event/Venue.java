package com.andrecoura.events.domain.event;

import com.andrecoura.events.domain.shared.Rules;

/** Value object: where the event happens. */
public record Venue(String name, String address) {

    public Venue {
        name = Rules.requiredText(name, 120, "venue name");
        address = Rules.requiredText(address, 255, "venue address");
    }
}
