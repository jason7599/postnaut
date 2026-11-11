package com.jason7599.postnaut.reply;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

interface ReplyChainViewRepository extends Repository<ReplyChainView, UUID> {

    List<ReplyChainView> findTop1000ByPostIdOrderByLastReplyAtDescIdDesc(UUID postId);

    @Query(value = """
        SELECT * FROM reply_chains_view
        WHERE replier_id = :replierId
          AND NOT is_poster_turn
          AND post_expires_at > NOW()
        ORDER BY last_reply_at DESC, id DESC
        LIMIT 1000
    """, nativeQuery = true)
    List<ReplyChainView> getReplierInbox(UUID replierId);
}
