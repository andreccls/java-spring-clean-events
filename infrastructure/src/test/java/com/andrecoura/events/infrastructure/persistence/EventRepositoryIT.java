package com.andrecoura.events.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.event.EventFilter;
import com.andrecoura.events.application.port.EventRepository;
import com.andrecoura.events.application.port.Transaction;
import com.andrecoura.events.domain.event.Event;
import com.andrecoura.events.domain.event.EventStatus;
import com.andrecoura.events.domain.event.Venue;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class EventRepositoryIT extends PersistenceIT {

    static final Instant BASE = Instant.parse("2031-03-01T12:00:00.123456Z");

    @Autowired
    EventRepository events;

    @Autowired
    Transaction transaction;

    Event event(String title, Instant start) {
        return Event.create(title, "desc", new Venue("Hall", "1 Street"), start, start.plus(2, ChronoUnit.HOURS), 10);
    }

    @Test
    void roundTripsEveryFieldIncludingMicroseconds() {
        Event saved = events.save(event("Round trip", BASE));

        Event loaded = events.findById(saved.id()).orElseThrow();

        assertThat(loaded).usingRecursiveComparison().isEqualTo(saved);
        assertThat(loaded.startsAt()).isEqualTo(BASE);
        assertThat(loaded.status()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    void savingAgainUpdatesTheSameRow() {
        Event saved = events.save(event("Before", BASE));
        saved.update("After", null, new Venue("Annex", "2 Street"), BASE, BASE.plusSeconds(60), 3);
        saved.publish(BASE.minusSeconds(1));

        events.save(saved);

        Event loaded = events.findById(saved.id()).orElseThrow();
        assertThat(loaded.title()).isEqualTo("After");
        assertThat(loaded.description()).isNull();
        assertThat(loaded.status()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM events", Integer.class)).isEqualTo(1);
    }

    @Test
    void findUnknownIsEmpty() {
        assertThat(events.findById(UUID.randomUUID())).isEmpty();
        // the locking read must run inside a transaction (port contract)
        assertThat(transaction.execute(() -> events.findByIdForUpdate(UUID.randomUUID()))).isEmpty();
    }

    @Test
    void searchFiltersByStatusAndPeriodOrderedByStart() {
        Event first = events.save(event("first", BASE));
        Event second = events.save(event("second", BASE.plus(1, ChronoUnit.DAYS)));
        Event third = events.save(event("third", BASE.plus(2, ChronoUnit.DAYS)));
        third.publish(BASE);
        events.save(third);

        var all = events.search(new EventFilter(null, null, null), PageRequest.of(null, null));
        assertThat(all.items()).extracting(Event::id).containsExactly(first.id(), second.id(), third.id());
        assertThat(all.total()).isEqualTo(3);

        assertThat(events.search(new EventFilter(EventStatus.PUBLISHED, null, null), PageRequest.of(null, null)).items())
                .extracting(Event::id).containsExactly(third.id());

        // from is inclusive, to is exclusive
        var period = events.search(
                new EventFilter(null, BASE.plus(1, ChronoUnit.DAYS), BASE.plus(2, ChronoUnit.DAYS)), PageRequest.of(null, null));
        assertThat(period.items()).extracting(Event::id).containsExactly(second.id());
    }

    @Test
    void searchPaginates() {
        for (int i = 0; i < 5; i++) {
            events.save(event("e" + i, BASE.plus(i, ChronoUnit.DAYS)));
        }

        var page = events.search(new EventFilter(null, null, null), PageRequest.of(2, 2));

        assertThat(page.items()).extracting(Event::title).containsExactly("e2", "e3");
        assertThat(page.total()).isEqualTo(5);
        assertThat(page.page()).isEqualTo(2);
    }

    @Test
    void deletes() {
        Event saved = events.save(event("gone", BASE));

        events.deleteById(saved.id());

        assertThat(events.findById(saved.id())).isEmpty();
    }
}
