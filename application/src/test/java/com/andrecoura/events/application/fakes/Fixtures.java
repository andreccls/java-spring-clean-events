package com.andrecoura.events.application.fakes;

import com.andrecoura.events.application.event.EventData;
import com.andrecoura.events.application.port.Transaction;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.function.Supplier;

public final class Fixtures {

    public static final Instant NOW = Instant.parse("2030-01-01T10:00:00Z");
    public static final Instant START = NOW.plusSeconds(10 * 86_400);
    public static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    /** Runs the work directly: in-memory fakes need no real transaction. */
    public static final Transaction DIRECT = new Transaction() {
        @Override
        public <T> T execute(Supplier<T> work) {
            return work.get();
        }
    };

    private Fixtures() {}

    public static EventData eventData(int capacity) {
        return new EventData("Java Meetup", "Talks", "Main Hall", "1 Example Street", START, START.plusSeconds(3 * 3600), capacity);
    }
}
