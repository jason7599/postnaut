CREATE VIEW reply_chains_view AS
SELECT
    rc.id,
    rc.post_id,
    rc.replier_id,
    p.content AS post_content,
    p.expires_at AS post_expires_at,
    rc.last_seq,
    rc.is_poster_turn,
    rc.last_seq > rc.poster_last_read_seq AS poster_has_unread,
    rc.last_seq > rc.replier_last_read_seq AS replier_has_unread,
    CASE WHEN p.reveal_to_friends
        THEN COALESCE(pf.nickname, ru.display_name)
    END AS replier_name_for_poster,
    CASE WHEN p.reveal_to_friends
        THEN COALESCE(rf.nickname, pu.display_name)
    END AS poster_name_for_replier,
    lr.content AS last_reply_content,
    lr.created_at AS last_reply_at
FROM reply_chains rc
JOIN posts p ON p.id = rc.post_id
JOIN replies lr ON rc.id = lr.chain_id AND rc.last_seq = lr.seq
LEFT JOIN friends pf ON pf.owner_id = p.poster_id AND pf.friend_id = rc.replier_id
LEFT JOIN users ru ON ru.id = pf.friend_id
LEFT JOIN friends rf ON rf.owner_id = rc.replier_id AND rf.friend_id = p.poster_id
LEFT JOIN users pu ON pu.id = rf.friend_id;