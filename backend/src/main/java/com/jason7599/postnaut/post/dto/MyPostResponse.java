package com.jason7599.postnaut.post.dto;

import java.time.Instant;
import java.util.UUID;

public record MyPostResponse(
        UUID id,
        String content,
        boolean revealToFriends,
        boolean replyEnabled,
        Instant createdAt,
        Instant expiresAt,
        int viewCount,
        int likeCount,
        int dislikeCount,
        int replyChainCount,
        int unreadChainCount,
        boolean isExpired       // FE could derive this using expiresAt, but let's have the backend be the only source of truth
) {
}