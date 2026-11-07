package com.jason7599.postnaut.common;

import org.springframework.data.domain.Window;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> items,
        String nextCursor   // null if end of page, else items.last.id
) {
    public static <E, T> PageResponse<T> fromWindow(Window<E> w, Function<E, T> map, Function<E, ?> cursorOf) {
        List<E> content = w.getContent();
        String next = w.hasNext() && !content.isEmpty() ? cursorOf.apply(content.getLast()).toString() : null;

        return new PageResponse<>(
                content.stream().map(map).toList(),
                next
        );
    }
}
