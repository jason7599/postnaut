CREATE VIEW my_posts AS
SELECT
    p.id,
    p.poster_id,
    p.content,
    p.reveal_to_friends,
    p.reply_enabled,
    p.client_id,
    p.created_at,
    p.expires_at,
    v.view_count,
    v.like_count,
    v.dislike_count,
    rc.reply_chain_count,
    rc.unread_chain_count,
    (p.expires_at <= NOW()) AS is_expired
FROM posts p
CROSS JOIN LATERAL (
    SELECT
        CAST(COUNT(*) AS int) AS view_count,
        CAST(COUNT(*) FILTER (WHERE reaction = 1) AS int) AS like_count,
        CAST(COUNT(*) FILTER (WHERE reaction = -1) AS int) AS dislike_count
    FROM views
    WHERE post_id = p.id
) v
CROSS JOIN LATERAL (
    SELECT
        CAST(COUNT(*) AS int) AS reply_chain_count,
        CAST(COUNT(*) FILTER (WHERE poster_last_read_seq < last_seq) AS int) AS unread_chain_count
    FROM reply_chains
    WHERE post_id = p.id
) rc;