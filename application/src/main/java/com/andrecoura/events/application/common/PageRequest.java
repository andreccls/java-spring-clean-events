package com.andrecoura.events.application.common;

/** 1-based page request. Out-of-range values are clamped, not rejected. */
public record PageRequest(int page, int size) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public static PageRequest of(Integer page, Integer size) {
        int p = page == null ? 1 : Math.max(1, page);
        int s = size == null ? DEFAULT_SIZE : Math.min(MAX_SIZE, Math.max(1, size));
        return new PageRequest(p, s);
    }

    public int offset() {
        return (page - 1) * size;
    }
}
