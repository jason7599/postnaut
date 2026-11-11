package com.jason7599.postnaut.reply.dto;

import java.time.Instant;
import java.util.UUID;

/** Replier view; Only sees chains where it is the replier's turn */
public record ReplierReplyChainResponse(
        UUID id,
        int lastSeq,
        boolean hasUnread,
        String postContent,
        String posterName,
        Instant postExpiresAt,
        String lastReplyContent,
        Instant lastReplyAt
) {
}
