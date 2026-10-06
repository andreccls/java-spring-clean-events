package com.andrecoura.events.web.event;

import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.event.CancelEvent;
import com.andrecoura.events.application.event.CreateEvent;
import com.andrecoura.events.application.event.DeleteEvent;
import com.andrecoura.events.application.event.EventFilter;
import com.andrecoura.events.application.event.EventView;
import com.andrecoura.events.application.event.FinishEvent;
import com.andrecoura.events.application.event.GetEvent;
import com.andrecoura.events.application.event.ListEvents;
import com.andrecoura.events.application.event.PublishEvent;
import com.andrecoura.events.application.event.UpdateEvent;
import com.andrecoura.events.domain.event.EventStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Input adapter: translates HTTP to use-case calls. No business rule lives here. */
@Tag(name = "Events")
@RestController
@RequestMapping("/events")
class EventController {

    private final CreateEvent create;
    private final GetEvent get;
    private final ListEvents list;
    private final UpdateEvent update;
    private final PublishEvent publish;
    private final CancelEvent cancel;
    private final FinishEvent finish;
    private final DeleteEvent delete;

    EventController(
            CreateEvent create,
            GetEvent get,
            ListEvents list,
            UpdateEvent update,
            PublishEvent publish,
            CancelEvent cancel,
            FinishEvent finish,
            DeleteEvent delete) {
        this.create = create;
        this.get = get;
        this.list = list;
        this.update = update;
        this.publish = publish;
        this.cancel = cancel;
        this.finish = finish;
        this.delete = delete;
    }

    @PostMapping
    ResponseEntity<EventView> create(@Valid @RequestBody EventRequest request) {
        EventView created = create.execute(request.toData());
        return ResponseEntity.created(URI.create("/events/" + created.id())).body(created);
    }

    @GetMapping
    Page<EventView> list(
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return list.execute(new EventFilter(status, from, to), PageRequest.of(page, pageSize));
    }

    @GetMapping("/{id}")
    EventView get(@PathVariable UUID id) {
        return get.execute(id);
    }

    @PutMapping("/{id}")
    EventView update(@PathVariable UUID id, @Valid @RequestBody EventRequest request) {
        return update.execute(id, request.toData());
    }

    @PostMapping("/{id}/publish")
    EventView publish(@PathVariable UUID id) {
        return publish.execute(id);
    }

    @PostMapping("/{id}/cancel")
    EventView cancel(@PathVariable UUID id) {
        return cancel.execute(id);
    }

    @PostMapping("/{id}/finish")
    EventView finish(@PathVariable UUID id) {
        return finish.execute(id);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        delete.execute(id);
        return ResponseEntity.noContent().build();
    }
}
