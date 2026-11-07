package com.jason7599.postnaut.reply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

interface ReplyRepository extends JpaRepository<ReplyEntity, ReplyEntity.Id> {

    String REPLY_CHAIN_SELECT_HEAD = """
        SELECT
            rc.id,
            rc.post_id          AS postId,
            p.content           AS postContent,
            rc.last_seq         AS lastSeq,
    """;

    String AS_POSTER_COLUMNS = """
        rc.poster_last_read_seq AS myLastReadSeq,
        rc.is_poster_turn       AS isMyTurn,
    """;

    String AS_REPLIER_COLUMNS = """
        rc.replier_last_read_seq AS myLastReadSeq,
        NOT rc.is_poster_turn    AS isMyTurn,
    """;

    String REPLY_CHAIN_SELECT_TAIL = """
            NULL::text          AS friendNickname, -- TODO: fix later when i add friends
            lr.content          AS lastReplyContent,
            lr.created_at       AS lastReplyAt
        FROM reply_chains rc
        JOIN posts p    ON p.id = rc.post_id
        JOIN replies lr ON lr.chain_id = rc.id AND lr.seq = rc.last_seq
    """;

    String KEYSET_PAGE = """
            AND (lr.created_at, rc.id) < (:cursorAt, :cursorId)
        ORDER BY lr.created_at DESC, rc.id DESC
        LIMIT :limit
    """;

    @Query(value = """
        INSERT INTO reply_chains (post_id, replier_id)
        VALUES (:postId, :replierId)
        ON CONFLICT (post_id, replier_id)
        DO UPDATE SET last_seq = reply_chains.last_seq -- no-op touch
        RETURNING id
    """, nativeQuery = true)
    UUID upsertReplyChain(UUID postId, UUID replierId);

    @Query(value = """
        SELECT *
        FROM reply_chains
        WHERE id = :chain_id
        FOR NO KEY UPDATE
    """, nativeQuery = true)
    Optional<ReplyChain> lockReplyChain(UUID chainId);

    @Query(value = """
        SELECT *
        FROM replies
        WHERE chain_id = :chainId
            AND seq = :seq
    """, nativeQuery = true)
    Optional<ReplyEntity> findByChainIdAndSeq(UUID chainId, int seq);

    @Query(value = """
        SELECT *
        FROM replies
        WHERE chain_id = :chainId
            AND client_id = :clientId
    """, nativeQuery = true)
    Optional<ReplyEntity> findByChainIdAndClientId(UUID chainId, UUID clientId);

    @Query(value = """
        WITH existing AS (
            SELECT *
            FROM replies
            WHERE chain_id = :chainId
                AND client_id = :clientId
        ),
        bumped AS (
            UPDATE reply_chains
            SET last_seq = last_seq + 1,
                poster_last_read_seq = CASE WHEN :fromPoster THEN last_seq + 1 ELSE poster_last_read_seq END,
                replier_last_read_seq = CASE WHEN :fromPoster THEN replier_last_read_seq ELSE last_seq + 1 END
            WHERE id = :chainId
                AND NOT EXISTS (SELECT 1 FROM existing) -- no bump if exists
            RETURNING id, last_seq
        ),
        inserted AS (
            INSERT INTO replies (chain_id, seq, content, client_id)
            SELECT id, last_seq, :content, :clientId
            FROM bumped
            RETURNING *
        )
        SELECT * FROM inserted
        UNION ALL
        SELECT * FROM existing
    """, nativeQuery = true)
    Optional<ReplyEntity> appendReply(UUID chainId, String content, boolean fromPoster, UUID clientId);

//    @Query(value = REPLY_CHAIN_SELECT_HEAD + AS_POSTER_COLUMNS + REPLY_CHAIN_SELECT_TAIL + """
//    WHERE rc.post_id = :postId
//    """ + KEYSET_PAGE, nativeQuery = true)
//    List<ReplyChainResponse> getReplyChainsAsPoster(
//            UUID postId, Instant cursorAt, UUID cursorId, int limit);
//
//    @Query(value = REPLY_CHAIN_SELECT_HEAD + AS_REPLIER_COLUMNS + REPLY_CHAIN_SELECT_TAIL + """
//    WHERE rc.replier_id = :replierId
//      AND p.expires_at > NOW()
//    """ + KEYSET_PAGE, nativeQuery = true)
//    List<ReplyChainResponse> getReplyChainsAsReplier(
//            UUID replierId, Instant cursorAt, UUID cursorId, int limit);
}
