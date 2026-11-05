package com.jason7599.postnaut.post.dto;

import com.jason7599.postnaut.post.PostConstraints;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Duration;
import java.util.UUID;

public record CreatePostRequest(
        @NotBlank(message = "content is required") // change later
        @Size(
                max = PostConstraints.CONTENT_MAX_LENGTH,
                message = PostConstraints.CONTENT_LENGTH_MESSAGE
        )
        String content,

        @NotNull(message = "revealToFriends is required")
        Boolean revealToFriends,

        @NotNull(message = "replyEnabled is required")
        Boolean replyEnabled,

        @NotNull(message = "clientId is required")
        UUID clientId,

        @NotNull(message = "lifespan is required")
        Duration lifespan
) {
    public CreatePostRequest {
        if (content != null) {
            content = content.strip();
        }
    }

    @AssertTrue(message = PostConstraints.LIFESPAN_MESSAGE)
    private boolean isLifespanValid() {
        // allow null so that it catches the notnull message instead
        return lifespan == null || PostConstraints.isValidLifespan(lifespan);
    }
}
