CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE users (
    id              UUID        NOT NULL DEFAULT uuidv7(),
    username        CITEXT      NOT NULL,
    display_name    VARCHAR(32) NOT NULL,
    password_hash   TEXT        NOT NULL,
    friend_code     TEXT        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_friend_code UNIQUE (friend_code),
    CONSTRAINT chk_users_username_format CHECK (username ~ '^[a-z0-9_]{3,20}$'),
    CONSTRAINT chk_users_display_name_not_blank CHECK (display_name ~ '\S')
);

CREATE TABLE posts (
    id                  UUID         NOT NULL DEFAULT uuidv7(),
    poster_id           UUID         NOT NULL,
    content             VARCHAR(500) NOT NULL,
    reveal_to_friends   BOOLEAN      NOT NULL,
    reply_enabled       BOOLEAN      NOT NULL,
    client_id           UUID         NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    expires_at          TIMESTAMPTZ  NOT NULL,

    -- denormalized fields
    view_count          INT          NOT NULL DEFAULT 0,
    like_count          INT          NOT NULL DEFAULT 0,
    dislike_count       INT          NOT NULL DEFAULT 0,

    CONSTRAINT pk_posts PRIMARY KEY (id),
    CONSTRAINT fk_posts_poster_id FOREIGN KEY (poster_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_posts_poster_client UNIQUE (poster_id, client_id), -- also indexes poster_id
    CONSTRAINT chk_posts_content_not_blank CHECK (content ~ '\S'),
    CONSTRAINT chk_posts_expires_after_created CHECK (created_at < expires_at)
);
CREATE INDEX idx_posts_poster_id_expires_at ON posts (poster_id, expires_at);
CREATE INDEX idx_posts_poster_id_created_at_id ON posts (poster_id, created_at DESC, id DESC);

CREATE TABLE views (
    user_id     UUID     NOT NULL,
    post_id     UUID     NOT NULL,
    reaction    SMALLINT NOT NULL DEFAULT 0, -- -1 for dislike, 0 for none, 1 for like

    CONSTRAINT pk_views PRIMARY KEY (user_id, post_id),
    CONSTRAINT fk_views_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_views_post_id FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT chk_views_reaction_range CHECK (reaction BETWEEN -1 AND 1)
);
CREATE INDEX idx_views_post_id ON views (post_id);

-- (post_id, replier_id) is enough as a key, but shipping replier_id in the API would be leakage
CREATE TABLE reply_chain (
    id          UUID NOT NULL DEFAULT uuidv7(),
    post_id     UUID NOT NULL,
    replier_id  UUID NOT NULL,
    last_seq    INT  NOT NULL DEFAULT 0,

    CONSTRAINT pk_reply_chain PRIMARY KEY (id),
    CONSTRAINT fk_reply_chain_post_id FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_reply_chain_replier_id FOREIGN KEY (replier_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_reply_chain_post_replier UNIQUE (post_id, replier_id) -- also indexes post_id
);
CREATE INDEX idx_reply_chain_replier_id ON reply_chain (replier_id);

CREATE TABLE replies (
    chain_id    UUID         NOT NULL,
    seq         INT          NOT NULL, -- starts at 1
    from_poster BOOLEAN      GENERATED ALWAYS AS (seq % 2 = 0), -- replier always goes first
    content     VARCHAR(300) NOT NULL,
    client_id   UUID         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_replies PRIMARY KEY (chain_id, seq),
    CONSTRAINT fk_replies_chain_id FOREIGN KEY (chain_id) REFERENCES reply_chain (id) ON DELETE CASCADE,
    CONSTRAINT uq_replies_chain_client UNIQUE (chain_id, client_id),
    CONSTRAINT chk_replies_seq_positive CHECK (seq > 0),
    CONSTRAINT chk_replies_content_not_blank CHECK (char_length(btrim(content)) > 0)
);

CREATE TABLE friend_requests (
    id              UUID        NOT NULL DEFAULT uuidv7(),
    requester_id    UUID        NOT NULL,
    requestee_id    UUID        NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_friend_requests PRIMARY KEY (id),
    CONSTRAINT fk_friend_requests_requester_id FOREIGN KEY (requester_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_friend_requests_requestee_id FOREIGN KEY (requestee_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_friend_requests_not_self CHECK (requester_id <> requestee_id)
);
-- expression-based uniqueness can't be a constraint, so it stays an index
CREATE UNIQUE INDEX uq_friend_requests_pair
    ON friend_requests (LEAST(requester_id, requestee_id), GREATEST(requester_id, requestee_id));
CREATE INDEX idx_friend_requests_requester_id ON friend_requests (requester_id);
CREATE INDEX idx_friend_requests_requestee_id ON friend_requests (requestee_id);

CREATE TABLE friends (
    id              UUID        NOT NULL DEFAULT uuidv7(),
    owner_id        UUID        NOT NULL,
    friend_id       UUID        NOT NULL,
    nickname        VARCHAR(32),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_friends PRIMARY KEY (id),
    CONSTRAINT fk_friends_owner_id FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_friends_friend_id FOREIGN KEY (friend_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_friends_pair UNIQUE (owner_id, friend_id),
    CONSTRAINT uq_friends_nickname UNIQUE (owner_id, nickname),
    CONSTRAINT chk_friends_not_self CHECK (owner_id <> friend_id),
    CONSTRAINT chk_friends_nickname_not_blank CHECK (nickname IS NULL OR char_length(btrim(nickname)) > 0),
    CONSTRAINT fk_friends_symmetric FOREIGN KEY (friend_id, owner_id) REFERENCES friends (owner_id, friend_id) ON DELETE CASCADE DEFERRABLE INITIALLY DEFERRED
);
CREATE INDEX idx_friends_friend_id ON friends (friend_id);

CREATE TABLE notifications (
    id                  UUID        NOT NULL DEFAULT uuidv7(),
    user_id             UUID        NOT NULL,
    type                TEXT        NOT NULL,
    post_id             UUID,
    chain_id            UUID,
    friend_request_id   UUID,
    read_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_post_id FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_chain_id FOREIGN KEY (chain_id) REFERENCES reply_chain (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_friend_request_id FOREIGN KEY (friend_request_id) REFERENCES friend_requests (id) ON DELETE CASCADE,
    CONSTRAINT chk_notifications_fields CHECK (
        (type = 'FRIEND_POST'
            AND post_id IS NOT NULL AND chain_id IS NULL        AND friend_request_id IS NULL) OR
        (type = 'REPLY'
            AND post_id IS NULL     AND chain_id IS NOT NULL    AND friend_request_id IS NULL) OR
        (type = 'FRIEND_REQUEST'
            AND post_id IS NULL     AND chain_id IS NULL        AND friend_request_id IS NOT NULL)
        )
);
-- for the notification list per user
CREATE INDEX idx_notifications_user_id_created_at ON notifications (user_id, created_at DESC);
-- partial indices for cascade
CREATE INDEX idx_notifications_post_id    ON notifications (post_id)           WHERE post_id IS NOT NULL;
CREATE INDEX idx_notifications_chain_id   ON notifications (chain_id)          WHERE chain_id IS NOT NULL;
CREATE INDEX idx_notifications_request_id ON notifications (friend_request_id) WHERE friend_request_id IS NOT NULL;