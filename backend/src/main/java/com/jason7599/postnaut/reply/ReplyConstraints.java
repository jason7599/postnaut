package com.jason7599.postnaut.reply;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReplyConstraints {

    public static final int REPLY_MAX_LENGTH = 300;
    public static final String REPLY_LENGTH_MESSAGE
            = "content must be between 1 and "
            + REPLY_MAX_LENGTH
            + " characters";

    public static final String UQ_CLIENT_ID = "uq_replies_chain_client";
}
