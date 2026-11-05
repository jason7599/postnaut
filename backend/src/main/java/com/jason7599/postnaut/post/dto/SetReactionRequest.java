package com.jason7599.postnaut.post.dto;

import com.jason7599.postnaut.post.PostReaction;
import jakarta.validation.constraints.NotNull;

public record SetReactionRequest(
        @NotNull(message = "reaction is required")
        PostReaction reaction
) {
}
