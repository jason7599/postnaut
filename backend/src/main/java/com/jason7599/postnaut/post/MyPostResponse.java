package com.jason7599.postnaut.post;

import java.time.Instant;
import java.util.UUID;

public record MyPostResponse(
        UUID id,
        String content,
        boolean revealToFriends,
        boolean replyEnabled,
        Instant createdAt,
        Instant expiresAt
) {
    MyPostResponse(PostEntity e) {
        this(
                e.getId(),
                e.getContent(),
                e.isRevealToFriends(),
                e.isReplyEnabled(),
                e.getCreatedAt(),
                e.getExpiresAt()
        );
    }
}
