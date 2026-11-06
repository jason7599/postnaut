package com.jason7599.postnaut.reply.dto;

import com.jason7599.postnaut.reply.ReplyConstraints;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReplyRequest(
        @NotBlank(message = "content is required")
        @Size(
                max = ReplyConstraints.REPLY_MAX_LENGTH,
                message = ReplyConstraints.REPLY_LENGTH_MESSAGE
        )
        String content,

        @NotNull(message = "clientId is required")
        UUID clientId
) {
    public CreateReplyRequest {
        if (content != null) {
            content = content.strip();
        }
    }
}
