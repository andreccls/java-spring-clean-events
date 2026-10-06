package com.andrecoura.events.domain.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andrecoura.events.domain.shared.ConflictException;
import com.andrecoura.events.domain.shared.DomainException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EventTest {

    static final Instant NOW = Instant.parse("2030-01-01T10:00:00Z");
    static final Instant START = NOW.plus(Duration.ofDays(10));
    static final Instant END = START.plus(Duration.ofHours(3));
    static final Venue VENUE = new Venue("Main Hall", "1 Example Street");

    static Event draft(int capacity) {
        return Event.create("Java Meetup", "Monthly talks", VENUE, START, END, capacity);
    }

    static Event published(int capacity) {
        Event event = draft(capacity);
        event.publish(NOW);
        return event;
    }

    @Nested
    class Creation {
        @Test
        void startsAsDraftWithGivenData() {
            Event event = draft(50);

            assertThat(event.id()).isNotNull();
            assertThat(event.status()).isEqualTo(EventStatus.DRAFT);
            assertThat(event.title()).isEqualTo("Java Meetup");
            assertThat(event.description()).isEqualTo("Monthly talks");
            assertThat(event.venue()).isEqualTo(VENUE);
            assertThat(event.startsAt()).isEqualTo(START);
            assertThat(event.endsAt()).isEqualTo(END);
            assertThat(event.capacity()).isEqualTo(50);
        }

        @Test
        void trimsTitleAndAllowsMissingDescription() {
            Event event = Event.create("  Spaced  ", null, VENUE, START, END, 0);

            assertThat(event.title()).isEqualTo("Spaced");
            assertThat(event.description()).isNull();
            assertThat(event.capacity()).isZero();
        }

        @Test
        void rejectsMissingTitle() {
            assertThatThrownBy(() -> Event.create(null, null, VENUE, START, END, 1)).isInstanceOf(DomainException.class);
            assertThatThrownBy(() -> Event.create("  ", null, VENUE, START, END, 1)).isInstanceOf(DomainException.class);
        }

        @Test
        void rejectsTooLongTitleAndDescription() {
            assertThatThrownBy(() -> Event.create("x".repeat(201), null, VENUE, START, END, 1))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("title");
            assertThatThrownBy(() -> Event.create("ok", "x".repeat(2001), VENUE, START, END, 1))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("description");
        }

        @Test
        void rejectsMissingVenueOrDates() {
            assertThatThrownBy(() -> Event.create("ok", null, null, START, END, 1)).isInstanceOf(DomainException.class);
            assertThatThrownBy(() -> Event.create("ok", null, VENUE, null, END, 1)).isInstanceOf(DomainException.class);
            assertThatThrownBy(() -> Event.create("ok", null, VENUE, START, null, 1)).isInstanceOf(DomainException.class);
        }

        @Test
        void rejectsEndNotAfterStart() {
            assertThatThrownBy(() -> Event.create("ok", null, VENUE, START, START, 1))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("end");
            assertThatThrownBy(() -> Event.create("ok", null, VENUE, START, START.minusSeconds(1), 1))
                    .isInstanceOf(DomainException.class);
        }

        @Test
        void rejectsNegativeCapacity() {
            assertThatThrownBy(() -> draft(-1)).isInstanceOf(DomainException.class).hasMessageContaining("capacity");
        }
    }

    @Nested
    class Publishing {
        @Test
        void publishesDraftWithFutureStartAndCapacity() {
            Event event = draft(10);

            event.publish(NOW);

            assertThat(event.status()).isEqualTo(EventStatus.PUBLISHED);
        }

        @Test
        void requiresPositiveCapacity() {
            Event event = draft(0);

            assertThatThrownBy(() -> event.publish(NOW)).isInstanceOf(DomainException.class).hasMessageContaining("capacity");
            assertThat(event.status()).isEqualTo(EventStatus.DRAFT);
        }

        @Test
        void requiresFutureStart() {
            Event event = draft(10);

            assertThatThrownBy(() -> event.publish(START)).isInstanceOf(DomainException.class).hasMessageContaining("future");
            assertThatThrownBy(() -> event.publish(START.plusSeconds(1))).isInstanceOf(DomainException.class);
        }

        @Test
        void onlyDraftCanBePublished() {
            Event event = published(10);

            assertThatThrownBy(() -> event.publish(NOW)).isInstanceOf(ConflictException.class);
        }
    }

    @Nested
    class Lifecycle {
        @Test
        void draftAndPublishedCanBeCancelled() {
            Event fromDraft = draft(10);
            Event fromPublished = published(10);

            fromDraft.cancel();
            fromPublished.cancel();

            assertThat(fromDraft.status()).isEqualTo(EventStatus.CANCELLED);
            assertThat(fromPublished.status()).isEqualTo(EventStatus.CANCELLED);
        }

        @Test
        void cancelledAndFinishedCannotBeCancelledAgain() {
            Event cancelled = draft(10);
            cancelled.cancel();
            Event finished = published(10);
            finished.finish();

            assertThatThrownBy(cancelled::cancel).isInstanceOf(ConflictException.class);
            assertThatThrownBy(finished::cancel).isInstanceOf(ConflictException.class);
        }

        @Test
        void onlyPublishedCanFinish() {
            Event event = published(10);

            event.finish();

            assertThat(event.status()).isEqualTo(EventStatus.FINISHED);
            assertThatThrownBy(() -> draft(10).finish()).isInstanceOf(ConflictException.class);
        }
    }

    @Nested
    class Updating {
        @Test
        void draftAndPublishedCanBeEdited() {
            Event event = published(10);
            Venue other = new Venue("Annex", "2 Example Street");

            event.update("New title", "New description", other, START.plusSeconds(60), END.plusSeconds(60), 20);

            assertThat(event.title()).isEqualTo("New title");
            assertThat(event.description()).isEqualTo("New description");
            assertThat(event.venue()).isEqualTo(other);
            assertThat(event.startsAt()).isEqualTo(START.plusSeconds(60));
            assertThat(event.endsAt()).isEqualTo(END.plusSeconds(60));
            assertThat(event.capacity()).isEqualTo(20);
            assertThat(event.status()).isEqualTo(EventStatus.PUBLISHED);
        }

        @Test
        void cancelledAndFinishedAreFrozen() {
            Event cancelled = draft(10);
            cancelled.cancel();
            Event finished = published(10);
            finished.finish();

            assertThatThrownBy(() -> cancelled.update("t", null, VENUE, START, END, 1)).isInstanceOf(ConflictException.class);
            assertThatThrownBy(() -> finished.update("t", null, VENUE, START, END, 1)).isInstanceOf(ConflictException.class);
        }

        @Test
        void keepsInvariantsOnUpdate() {
            Event event = draft(10);

            assertThatThrownBy(() -> event.update("t", null, VENUE, START, START, 1)).isInstanceOf(DomainException.class);
            assertThat(event.endsAt()).isEqualTo(END);
        }
    }

    @Nested
    class Registration {
        @Test
        void acceptsWhileThereIsRoom() {
            Event event = published(2);

            event.ensureCanRegister(0);
            event.ensureCanRegister(1);
        }

        @Test
        void refusesWhenFull() {
            Event event = published(2);

            assertThatThrownBy(() -> event.ensureCanRegister(2)).isInstanceOf(ConflictException.class).hasMessageContaining("full");
        }

        @Test
        void refusesWhenNotPublished() {
            assertThatThrownBy(() -> draft(2).ensureCanRegister(0)).isInstanceOf(ConflictException.class).hasMessageContaining("published");
        }
    }

    @Test
    void restoreRebuildsWithoutValidatingTransitions() {
        UUID id = UUID.randomUUID();

        Event event = Event.restore(id, "Old", null, VENUE, START, END, 5, EventStatus.FINISHED);

        assertThat(event.id()).isEqualTo(id);
        assertThat(event.status()).isEqualTo(EventStatus.FINISHED);
    }
}
