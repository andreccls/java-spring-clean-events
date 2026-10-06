package com.andrecoura.events.domain.participant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.andrecoura.events.domain.shared.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @Test
    void acceptsAndNormalizes() {
        assertThat(new Email(" A.B+c@Sub.Example.com ").value()).isEqualTo("a.b+c@sub.example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "plain", "a@b", "@b.com", "a@.com", "a b@c.com", "a@b@c.com"})
    void rejectsMalformed(String raw) {
        assertThatThrownBy(() -> new Email(raw)).isInstanceOf(DomainException.class);
    }

    @Test
    void rejectsNullAndTooLong() {
        assertThatThrownBy(() -> new Email(null)).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> new Email("a".repeat(250) + "@b.com")).isInstanceOf(DomainException.class);
    }
}
