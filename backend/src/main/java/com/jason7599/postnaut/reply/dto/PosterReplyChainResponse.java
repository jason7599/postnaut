package com.jason7599.postnaut.reply.dto;

import java.time.Instant;
import java.util.UUID;

/** Poster view; For listing reply chains on my post */
public record PosterReplyChainResponse(
        UUID id,
        int lastSeq,        // sent back on "mark read"
        boolean hasUnread,
        boolean isMyTurn,
        // Nickname that I set for the replier, falling back to their display name.
        // null if post has reaveal_to_friends disabled, or the replier isn't my friend.
        String replierName,
        String lastReplyContent,
        Instant lastReplyAt
) {
}
