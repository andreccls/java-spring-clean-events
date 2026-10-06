package com.andrecoura.events.application.common;

import java.util.List;
import java.util.function.Function;

public record Page<T>(List<T> items, int page, int size, long total) {

    public <R> Page<R> map(Function<T, R> mapper) {
        return new Page<>(items.stream().map(mapper).toList(), page, size, total);
    }
}
