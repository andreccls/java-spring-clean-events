package com.andrecoura.events.domain.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andrecoura.events.domain.shared.DomainException;
import org.junit.jupiter.api.Test;

class VenueTest {

    @Test
    void trimsFields() {
        Venue venue = new Venue("  Hall ", " 1 Street ");

        assertThat(venue.name()).isEqualTo("Hall");
        assertThat(venue.address()).isEqualTo("1 Street");
    }

    @Test
    void requiresNameAndAddress() {
        assertThatThrownBy(() -> new Venue(null, "a")).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new Venue(" ", "a")).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new Venue("n", null)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new Venue("n", " ")).isInstanceOf(DomainException.class);
    }

    @Test
    void limitsLength() {
        assertThatThrownBy(() -> new Venue("x".repeat(121), "a")).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new Venue("n", "x".repeat(256))).isInstanceOf(DomainException.class);
    }
}
