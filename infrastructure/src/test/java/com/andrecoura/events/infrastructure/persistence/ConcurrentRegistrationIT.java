package com.andrecoura.events.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.andrecoura.events.application.port.EventRepository;
import com.andrecoura.events.application.port.ParticipantRepository;
import com.andrecoura.events.application.port.RegistrationRepository;
import com.andrecoura.events.application.port.Transaction;
import com.andrecoura.events.application.registration.RegisterParticipant;
import com.andrecoura.events.domain.event.Event;
import com.andrecoura.events.domain.event.Venue;
import com.andrecoura.events.domain.participant.Email;
import com.andrecoura.events.domain.participant.Participant;
import com.andrecoura.events.domain.shared.ConflictException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** Proves the row lock: many simultaneous registrations never exceed the capacity. */
class ConcurrentRegistrationIT extends PersistenceIT {

    @Autowired EventRepository events;
    @Autowired ParticipantRepository participants;
    @Autowired RegistrationRepository registrations;
    @Autowired Transaction transaction;

    @Test
    void neverExceedsCapacityUnderConcurrency() throws Exception {
        int capacity = 3;
        int attempts = 12;
        Instant start = Instant.now().plusSeconds(86_400);
        Event event = Event.create("Hot", null, new Venue("v", "a"), start, start.plusSeconds(60), capacity);
        event.publish(Instant.now());
        UUID eventId = events.save(event).id();
        List<UUID> people = new ArrayList<>();
        for (int i = 0; i < attempts; i++) {
            people.add(participants.save(Participant.create("P" + i, new Email("p" + i + "@example.com"))).id());
        }
        RegisterParticipant register = new RegisterParticipant(events, participants, registrations, transaction, Clock.systemUTC());

        AtomicInteger accepted = new AtomicInteger();
        AtomicInteger refused = new AtomicInteger();
        CountDownLatch go = new CountDownLatch(1);
        try (ExecutorService pool = Executors.newFixedThreadPool(attempts)) {
            List<Future<?>> futures = new ArrayList<>();
            for (UUID person : people) {
                futures.add(pool.submit(() -> {
                    go.await();
                    try {
                        register.execute(eventId, person);
                        accepted.incrementAndGet();
                    } catch (ConflictException full) {
                        refused.incrementAndGet();
                    }
                    return null;
                }));
            }
            go.countDown();
            for (Future<?> f : futures) {
                f.get();
            }
        }

        assertThat(accepted.get()).isEqualTo(capacity);
        assertThat(refused.get()).isEqualTo(attempts - capacity);
        assertThat(registrations.countActiveByEvent(eventId)).isEqualTo(capacity);
    }
}
