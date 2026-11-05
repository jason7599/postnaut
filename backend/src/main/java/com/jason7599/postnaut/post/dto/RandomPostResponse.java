package com.jason7599.postnaut.post.dto;

import java.time.Instant;
import java.util.UUID;

public record RandomPostResponse(
        UUID id,
        String content,
        boolean replyEnabled,
        Instant createdAt,
        Instant expiresAt
) {
}
