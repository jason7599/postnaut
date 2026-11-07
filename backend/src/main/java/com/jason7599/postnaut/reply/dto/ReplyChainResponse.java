package com.jason7599.postnaut.reply.dto;

import java.time.Instant;
import java.util.UUID;

public record ReplyChainResponse(
        UUID id,
        UUID postId,
        String postContent,
        int lastSeq,
        int myLastReadSeq,
        boolean isMyTurn,
        // null if:
        // - post.reveal_to_friends is false
        // - or viewer is the poster, and replier is not a friend
        // - or viewer is the replier, and poster is not a friend
        String friendNickname,

        // Below can never be null, the existence of a reply chain means at least one reply
        String lastReplyContent,
        Instant lastReplyAt
) {
}
