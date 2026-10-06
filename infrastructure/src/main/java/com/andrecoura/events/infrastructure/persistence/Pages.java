package com.andrecoura.events.infrastructure.persistence;

import com.andrecoura.events.application.common.Page;
import com.andrecoura.events.application.common.PageRequest;
import java.util.function.Function;
import org.springframework.data.domain.Sort;

/** Translates between the application's paging types and Spring Data's. */
final class Pages {

    private Pages() {}

    static org.springframework.data.domain.PageRequest toSpring(PageRequest request, Sort sort) {
        return org.springframework.data.domain.PageRequest.of(request.page() - 1, request.size(), sort);
    }

    static <E, T> Page<T> toApplication(org.springframework.data.domain.Page<E> page, PageRequest request, Function<E, T> mapper) {
        return new Page<>(page.getContent().stream().map(mapper).toList(), request.page(), request.size(), page.getTotalElements());
    }
}
