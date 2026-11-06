package com.jason7599.postnaut.reply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

interface ReplyRepository extends JpaRepository<ReplyEntity, ReplyEntity.Id> {

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
}
