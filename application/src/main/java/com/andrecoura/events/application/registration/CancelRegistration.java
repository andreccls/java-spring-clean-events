package com.andrecoura.events.application.registration;

import com.andrecoura.events.application.UseCase;
import com.andrecoura.events.application.port.RegistrationRepository;
import com.andrecoura.events.domain.registration.Registration;
import com.andrecoura.events.domain.shared.NotFoundException;
import java.time.Clock;
import java.util.UUID;

/** Cancelling frees the seat: capacity checks count only ACTIVE registrations. */
@UseCase
public class CancelRegistration {

    private final RegistrationRepository registrations;
    private final Clock clock;

    public CancelRegistration(RegistrationRepository registrations, Clock clock) {
        this.registrations = registrations;
        this.clock = clock;
    }

    public RegistrationView execute(UUID registrationId) {
        Registration registration =
                registrations.findById(registrationId).orElseThrow(() -> new NotFoundException("Registration", registrationId));
        registration.cancel(clock.instant());
        return RegistrationView.from(registrations.save(registration));
    }
}
