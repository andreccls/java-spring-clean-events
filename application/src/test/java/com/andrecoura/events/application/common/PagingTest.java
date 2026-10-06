package com.andrecoura.events.application.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PagingTest {

    @Test
    void usesDefaultsWhenMissing() {
        assertThat(PageRequest.of(null, null)).isEqualTo(new PageRequest(1, 20));
    }

    @Test
    void keepsValuesInsideTheRange() {
        assertThat(PageRequest.of(3, 50)).isEqualTo(new PageRequest(3, 50));
    }

    @Test
    void clampsOutOfRangeValues() {
        assertThat(PageRequest.of(0, 0)).isEqualTo(new PageRequest(1, 1));
        assertThat(PageRequest.of(-5, 1000)).isEqualTo(new PageRequest(1, 100));
    }

    @Test
    void computesOffset() {
        assertThat(new PageRequest(3, 20).offset()).isEqualTo(40);
    }

    @Test
    void pageMapsItemsKeepingMetadata() {
        Page<Integer> page = new Page<>(List.of(1, 2), 2, 2, 5).map(i -> i * 10);

        assertThat(page.items()).containsExactly(10, 20);
        assertThat(page.page()).isEqualTo(2);
        assertThat(page.total()).isEqualTo(5);
    }
}
