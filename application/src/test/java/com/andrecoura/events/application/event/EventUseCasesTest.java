package com.andrecoura.events.application.event;

import static com.andrecoura.events.application.fakes.Fixtures.CLOCK;
import static com.andrecoura.events.application.fakes.Fixtures.DIRECT;
import static com.andrecoura.events.application.fakes.Fixtures.START;
import static com.andrecoura.events.application.fakes.Fixtures.eventData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.fakes.InMemoryEventRepository;
import com.andrecoura.events.application.fakes.InMemoryRegistrationRepository;
import com.andrecoura.events.domain.event.EventStatus;
import com.andrecoura.events.domain.registration.Registration;
import com.andrecoura.events.domain.shared.ConflictException;
import com.andrecoura.events.domain.shared.DomainException;
import com.andrecoura.events.domain.shared.NotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventUseCasesTest {

    final InMemoryEventRepository events = new InMemoryEventRepository();
    final InMemoryRegistrationRepository registrations = new InMemoryRegistrationRepository();
    final CreateEvent create = new CreateEvent(events);
    final GetEvent get = new GetEvent(events);
    final ListEvents list = new ListEvents(events);
    final UpdateEvent update = new UpdateEvent(events, registrations, DIRECT);
    final PublishEvent publish = new PublishEvent(events, CLOCK);
    final CancelEvent cancel = new CancelEvent(events);
    final FinishEvent finish = new FinishEvent(events);
    final DeleteEvent delete = new DeleteEvent(events);

    @Test
    void createsADraftAndReadsItBack() {
        EventView created = create.execute(eventData(10));

        assertThat(created.status()).isEqualTo(EventStatus.DRAFT);
        assertThat(created.venue().name()).isEqualTo("Main Hall");
        assertThat(get.execute(created.id())).isEqualTo(created);
    }

    @Test
    void createRejectsInvalidData() {
        assertThatThrownBy(() -> create.execute(new EventData(" ", null, "v", "a", START, START.plusSeconds(1), 1)))
                .isInstanceOf(DomainException.class);
        assertThat(events.count()).isZero();
    }

    @Test
    void getUnknownEventIsNotFound() {
        assertThatThrownBy(() -> get.execute(UUID.randomUUID())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void listsFilteredByStatusAndPeriodWithPaging() {
        EventView a = create.execute(eventData(10));
        EventView b = create.execute(new EventData("Later", null, "v", "a", START.plusSeconds(86_400), START.plusSeconds(90_000), 5));
        publish.execute(a.id());

        assertThat(list.execute(new EventFilter(null, null, null), PageRequest.of(null, null)).items())
                .extracting(EventView::id).containsExactly(a.id(), b.id());
        assertThat(list.execute(new EventFilter(EventStatus.PUBLISHED, null, null), PageRequest.of(null, null)).items())
                .extracting(EventView::id).containsExactly(a.id());
        assertThat(list.execute(new EventFilter(null, START.plusSeconds(1), START.plusSeconds(86_401)), PageRequest.of(null, null)).items())
                .extracting(EventView::id).containsExactly(b.id());
        assertThat(list.execute(new EventFilter(null, START, START.plusSeconds(86_400)), PageRequest.of(null, null)).items())
                .extracting(EventView::id).containsExactly(a.id());

        var page2 = list.execute(new EventFilter(null, null, null), PageRequest.of(2, 1));
        assertThat(page2.items()).extracting(EventView::id).containsExactly(b.id());
        assertThat(page2.total()).isEqualTo(2);
    }

    @Test
    void updatesAnEvent() {
        EventView created = create.execute(eventData(10));

        EventView updated = update.execute(created.id(), new EventData("Renamed", null, "Annex", "2 St", START, START.plusSeconds(60), 30));

        assertThat(updated.title()).isEqualTo("Renamed");
        assertThat(updated.capacity()).isEqualTo(30);
        assertThat(get.execute(created.id()).venue().name()).isEqualTo("Annex");
    }

    @Test
    void updateUnknownEventIsNotFound() {
        assertThatThrownBy(() -> update.execute(UUID.randomUUID(), eventData(1))).isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateCannotShrinkCapacityBelowActiveRegistrations() {
        EventView created = create.execute(eventData(10));
        registrations.save(Registration.register(created.id(), UUID.randomUUID(), START));
        registrations.save(Registration.register(created.id(), UUID.randomUUID(), START));

        assertThatThrownBy(() -> update.execute(created.id(), eventData(1))).isInstanceOf(ConflictException.class);
        assertThat(update.execute(created.id(), eventData(2)).capacity()).isEqualTo(2);
    }

    @Test
    void updateIsRefusedOnCancelledEvents() {
        EventView created = create.execute(eventData(10));
        cancel.execute(created.id());

        assertThatThrownBy(() -> update.execute(created.id(), eventData(5))).isInstanceOf(ConflictException.class);
    }

    @Test
    void publishCancelAndFinishFollowTheLifecycle() {
        EventView a = create.execute(eventData(10));
        EventView b = create.execute(eventData(10));

        assertThat(publish.execute(a.id()).status()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(finish.execute(a.id()).status()).isEqualTo(EventStatus.FINISHED);
        assertThat(cancel.execute(b.id()).status()).isEqualTo(EventStatus.CANCELLED);
    }

    @Test
    void lifecycleOperationsOnUnknownEventAreNotFound() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> publish.execute(unknown)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> cancel.execute(unknown)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> finish.execute(unknown)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> delete.execute(unknown)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void publishValidatesCapacity() {
        EventView noCapacity = create.execute(eventData(0));

        assertThatThrownBy(() -> publish.execute(noCapacity.id())).isInstanceOf(DomainException.class);
    }

    @Test
    void deletesOnlyDrafts() {
        EventView draft = create.execute(eventData(10));
        EventView published = create.execute(eventData(10));
        publish.execute(published.id());

        delete.execute(draft.id());

        assertThat(events.count()).isEqualTo(1);
        assertThatThrownBy(() -> delete.execute(published.id())).isInstanceOf(ConflictException.class);
    }
}
