package com.jason7599.postnaut.post;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostConstraints {

    public static final int CONTENT_MAX_LENGTH = 500;
    public static final String CONTENT_LENGTH_MESSAGE
            = "Content must be between 1 and "
            + CONTENT_MAX_LENGTH
            + " characters";

    private static final Set<Duration> LIFESPAN_OPTIONS = Set.of(
            Duration.ofHours(1),
            Duration.ofHours(6),
            Duration.ofHours(12),
            Duration.ofDays(1),
            Duration.ofDays(3),
            Duration.ofDays(5),
            Duration.ofDays(7)
    );

    public static boolean isValidLifespan(Duration lifespan) {
        return lifespan != null && LIFESPAN_OPTIONS.contains(lifespan);
    }

    public static final String LIFESPAN_MESSAGE =
            "Lifespan must be one of: PT1H, PT6H, PT12H, P1D, P3D, P5D, P7D";

    public static final String UQ_POSTER_CLIENT = "uq_posts_poster_client";

    public static final int MAX_LIVE_POSTS = 10;
}
