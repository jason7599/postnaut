# Denormalization on Posts

```sql
CREATE TABLE views (
    post_id     UUID     NOT NULL,
    user_id     UUID     NOT NULL,
    reaction    SMALLINT NOT NULL DEFAULT 0, -- -1 for dislike, 0 for none, 1 for like

    ...
);

CREATE TABLE posts (
    ...

    -- denormalized fields
    view_count          INT          NOT NULL DEFAULT 0,
    like_count          INT          NOT NULL DEFAULT 0,
    dislike_count       INT          NOT NULL DEFAULT 0,

    ...

    CONSTRAINT chk_posts_view_count_non_negative CHECK (view_count >= 0),
    CONSTRAINT chk_posts_like_count_non_negative CHECK (like_count >= 0),
    CONSTRAINT chk_posts_dislike_count_non_negative CHECK (dislike_count >= 0)
);
```

That was the original idea. But this is kinda getting silly. Imagine what a `likePost` operation has to do:

1. Update `reaction` on `views`.
1-1. For some reason maybe the `views` row didn't get inserted! So make it an "insert or update" operation.
2. If the reaction was already set before, no-op.
3. If not:
3-1. If previous reaction was `-1`(dislike), decrement dislike count, increment like count.
3-2. If previous reaction was `0`(none), just increment like count.

I mean.. it is doable. I can write a crazy omega query that does this in one atomic go, but.. it's getting silly.

Especially considering the posts are ephemeral. So yeah, this was a very fun thought experiment, and I can definitely see needing to do this in future schemas, but not here. 