package com.jason7599.postnaut.post;

import jakarta.validation.constraints.NotNull;

public record SetReactionRequest(
        @NotNull(message = "reaction is required")
        PostReaction reaction
) {
}
