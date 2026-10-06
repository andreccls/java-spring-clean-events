package com.andrecoura.events.domain.shared;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExceptionsTest {

    @Test
    void conflictAndNotFoundAreDomainExceptions() {
        assertThat(new ConflictException("c")).isInstanceOf(DomainException.class).hasMessage("c");
        assertThat(new NotFoundException("Event", "42")).isInstanceOf(DomainException.class).hasMessage("Event 42 not found");
    }
}
