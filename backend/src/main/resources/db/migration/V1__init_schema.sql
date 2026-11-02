CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuidv7(),
    username        CITEXT NOT NULL UNIQUE CONSTRAINT chk_users_username_length CHECK (char_length(username) BETWEEN 3 AND 32),
    display_name    VARCHAR(32) NOT NULL CONSTRAINT chk_users_display_name_not_blank CHECK (char_length(btrim(display_name)) > 0),
    password_hash   TEXT NOT NULL,
    friend_code     TEXT NOT NULL UNIQUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE posts (
    id                  UUID PRIMARY KEY DEFAULT uuidv7(),
    poster_id           UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content             VARCHAR(500) NOT NULL CONSTRAINT chk_posts_content_not_blank CHECK (char_length(btrim(content)) > 0),
    reveal_to_friends   BOOLEAN NOT NULL,
    reply_enabled       BOOLEAN NOT NULL,
    client_id           UUID NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at          TIMESTAMPTZ NOT NULL CONSTRAINT chk_posts_expires_at CHECK (created_at < expires_at),

    -- denormalized fields
    view_count          INT NOT NULL DEFAULT 0,
    like_count          INT NOT NULL DEFAULT 0,
    dislike_count       INT NOT NULL DEFAULT 0,

    UNIQUE (poster_id, client_id) -- also creates an index for poster_id
);
CREATE INDEX idx_posts_expires_at ON posts(expires_at);

CREATE TABLE views (
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id     UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    reaction    SMALLINT NOT NULL DEFAULT 0 CONSTRAINT chk_views_reaction_range CHECK (reaction BETWEEN -1 AND 1), -- -1 for dislike, 0 for none, 1 for like

    PRIMARY KEY (user_id, post_id)
);
CREATE INDEX idx_views_post_id ON views(post_id);

-- (post_id, replier_id) is enough as a key, but shipping replier_id in the API would be leakage
CREATE TABLE reply_chain (
    id          UUID PRIMARY KEY DEFAULT uuidv7(),
    post_id     UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    replier_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    last_seq    INT NOT NULL DEFAULT 0,

    UNIQUE (post_id, replier_id) -- also creates an index
);
CREATE INDEX idx_reply_chain_replier_id ON reply_chain(replier_id);

CREATE TABLE replies (
    chain_id    UUID NOT NULL REFERENCES reply_chain(id) ON DELETE CASCADE,
    seq         INT NOT NULL CONSTRAINT replies_seq_positive CHECK (seq > 0), -- starts at 1
    from_poster BOOLEAN GENERATED ALWAYS AS (seq % 2 = 0), -- replier always goes first
    content     VARCHAR(300) NOT NULL CONSTRAINT chk_replies_content_not_blank CHECK (char_length(btrim(content)) > 0),
    client_id   UUID NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (chain_id, seq),
    UNIQUE (chain_id, client_id)
);

CREATE TABLE friend_requests (
    id              UUID PRIMARY KEY DEFAULT uuidv7(),
    requester_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    requestee_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_friend_requests_not_self CHECK (requester_id <> requestee_id)
);
CREATE UNIQUE INDEX uq_friend_requests_pair ON friend_requests(LEAST(requester_id, requestee_id), GREATEST(requester_id, requestee_id));
CREATE INDEX idx_friend_requests_requester_id ON friend_requests(requester_id);
CREATE INDEX idx_friend_requests_requestee_id ON friend_requests(requestee_id);

CREATE TABLE friends (
    id              UUID PRIMARY KEY DEFAULT uuidv7(),
    owner_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    friend_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    nickname        VARCHAR(32) CONSTRAINT chk_friends_nickname_not_blank CHECK (nickname IS NULL OR char_length(btrim(nickname)) > 0),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_friends_not_self CHECK (owner_id <> friend_id),
    CONSTRAINT uq_friends_pair UNIQUE (owner_id, friend_id),
    CONSTRAINT uq_friends_nickname UNIQUE (owner_id, nickname),
    CONSTRAINT fk_friends_symmetric FOREIGN KEY (friend_id, owner_id)
         REFERENCES friends(owner_id, friend_id) ON DELETE CASCADE
         DEFERRABLE INITIALLY DEFERRED
);
CREATE INDEX idx_friends_friend_id ON friends(friend_id);

CREATE TABLE notifications (
    id                  UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id             UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type                TEXT NOT NULL,
    post_id             UUID REFERENCES posts(id) ON DELETE CASCADE,
    chain_id            UUID REFERENCES reply_chain(id) ON DELETE CASCADE,
    friend_request_id   UUID REFERENCES friend_requests(id) ON DELETE CASCADE,
    read_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

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
CREATE INDEX idx_notifications_user_id_created_at ON notifications(user_id, created_at DESC);
-- partial indices for cascade
CREATE INDEX idx_notifications_post_id    ON notifications(post_id)           WHERE post_id IS NOT NULL;
CREATE INDEX idx_notifications_chain_id   ON notifications(chain_id)          WHERE chain_id IS NOT NULL;
CREATE INDEX idx_notifications_request_id ON notifications(friend_request_id) WHERE friend_request_id IS NOT NULL;
