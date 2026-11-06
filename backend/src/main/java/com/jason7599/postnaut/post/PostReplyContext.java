package com.jason7599.postnaut.post;

import java.util.UUID;

public record PostReplyContext(
        UUID posterId,
        boolean replyEnabled
) {
}
