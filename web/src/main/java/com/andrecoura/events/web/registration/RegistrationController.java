package com.andrecoura.events.web.registration;

import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import com.andrecoura.events.application.registration.CancelRegistration;
import com.andrecoura.events.application.registration.ListEventRegistrations;
import com.andrecoura.events.application.registration.RegisterParticipant;
import com.andrecoura.events.application.registration.RegistrationView;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Registrations")
@RestController
class RegistrationController {

    record RegisterRequest(@NotNull UUID participantId) {}

    private final RegisterParticipant register;
    private final CancelRegistration cancel;
    private final ListEventRegistrations list;

    RegistrationController(RegisterParticipant register, CancelRegistration cancel, ListEventRegistrations list) {
        this.register = register;
        this.cancel = cancel;
        this.list = list;
    }

    @PostMapping("/events/{eventId}/registrations")
    @ResponseStatus(HttpStatus.CREATED)
    RegistrationView register(@PathVariable UUID eventId, @Valid @RequestBody RegisterRequest request) {
        return register.execute(eventId, request.participantId());
    }

    @GetMapping("/events/{eventId}/registrations")
    Page<RegistrationView> list(
            @PathVariable UUID eventId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return list.execute(eventId, PageRequest.of(page, pageSize));
    }

    @PostMapping("/registrations/{id}/cancel")
    RegistrationView cancel(@PathVariable UUID id) {
        return cancel.execute(id);
    }
}
