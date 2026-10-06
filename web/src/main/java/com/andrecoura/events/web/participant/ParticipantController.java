package com.andrecoura.events.web.participant;

import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.participant.CreateParticipant;
import com.andrecoura.events.application.participant.DeleteParticipant;
import com.andrecoura.events.application.participant.GetParticipant;
import com.andrecoura.events.application.participant.ListParticipants;
import com.andrecoura.events.application.participant.ParticipantView;
import com.andrecoura.events.application.participant.UpdateParticipant;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
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

@Tag(name = "Participants")
@RestController
@RequestMapping("/participants")
class ParticipantController {

    private final CreateParticipant create;
    private final GetParticipant get;
    private final ListParticipants list;
    private final UpdateParticipant update;
    private final DeleteParticipant delete;

    ParticipantController(
            CreateParticipant create,
            GetParticipant get,
            ListParticipants list,
            UpdateParticipant update,
            DeleteParticipant delete) {
        this.create = create;
        this.get = get;
        this.list = list;
        this.update = update;
        this.delete = delete;
    }

    @PostMapping
    ResponseEntity<ParticipantView> create(@Valid @RequestBody ParticipantRequest request) {
        ParticipantView created = create.execute(request.toData());
        return ResponseEntity.created(URI.create("/participants/" + created.id())).body(created);
    }

    @GetMapping
    Page<ParticipantView> list(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer pageSize) {
        return list.execute(PageRequest.of(page, pageSize));
    }

    @GetMapping("/{id}")
    ParticipantView get(@PathVariable UUID id) {
        return get.execute(id);
    }

    @PutMapping("/{id}")
    ParticipantView update(@PathVariable UUID id, @Valid @RequestBody ParticipantRequest request) {
        return update.execute(id, request.toData());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable UUID id) {
        delete.execute(id);
        return ResponseEntity.noContent().build();
    }
}
