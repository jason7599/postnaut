package com.jason7599.postnaut.reply.dto;

import java.time.Instant;
import java.util.UUID;

public record ReplyResponse(
    UUID chainId,
    int seq,
    boolean fromPoster,
    String content,
    Instant createdAt
) {
}
