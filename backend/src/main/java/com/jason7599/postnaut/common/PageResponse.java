package com.jason7599.postnaut.common;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        boolean hasMore
) {
}
