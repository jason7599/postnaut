package com.jason7599.postnaut.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface PostRepository extends JpaRepository<PostEntity, UUID> {

    String MY_POST_SELECT = """
        SELECT
            p.id,
            p.content,
            p.reveal_to_friends,
            p.reply_enabled,
            p.created_at,
            p.expires_at,
            v.view_count,
            v.like_count,
            v.dislike_count,
            rc.reply_chain_count,
            rc.unread_chain_count,
            (p.expires_at <= NOW()) AS isExpired
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
        ) rc
    """;

    @Query(value = """
        INSERT INTO posts (poster_id, content, reveal_to_friends, reply_enabled, client_id, expires_at)
        VALUES (:posterId, :content, :revealToFriends, :replyEnabled, :clientId, :expiresAt)
        ON CONFLICT ON CONSTRAINT uq_posts_poster_client
        DO NOTHING
        RETURNING *
    """, nativeQuery = true)
    Optional<PostEntity> insertPost(
            UUID posterId,
            String content,
            boolean revealToFriends,
            boolean replyEnabled,
            UUID clientId,
            Instant expiresAt
    );

    /** for live post cap */
    long countByPosterIdAndExpiresAtAfter(UUID posterId, Instant after);

    @Query(value = MY_POST_SELECT + """
        WHERE p.poster_id = :userId
            AND p.id < :cursor
            AND (
                (:includeLive AND p.expires_at > NOW())
                OR (:includeExpired AND p.expires_at <= NOW())
            )
        ORDER BY p.id DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<MyPostResponse> getUserPosts(
            UUID userId,
            UUID cursor,
            boolean includeLive,
            boolean includeExpired,
            int limit
    );

    @Query(value = MY_POST_SELECT + """
        WHERE p.poster_id = :posterId
            AND p.client_id = :clientId
    """, nativeQuery = true)
    Optional<MyPostResponse> getUserPostByPosterIdAndClientId(UUID posterId, UUID clientId);

    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM posts
            WHERE id = :postId
                AND expires_at > NOW()
        )
    """, nativeQuery = true)
    boolean isPostActive(UUID postId);

    /** No-op if view row already exists, or post is expired */
    @Modifying
    @Query(value = """
        INSERT INTO views (post_id, user_id)
        SELECT id, :userId
        FROM posts
        WHERE id = :postId
            AND expires_at > NOW()
        ON CONFLICT (post_id, user_id) DO NOTHING
    """, nativeQuery = true)
    int insertView(UUID postId, UUID userId);

    /** No-op if already exists or post is expired */
    @Modifying
    @Query(value = """
        INSERT INTO views (post_id, user_id, reaction)
        SELECT id, :userId, :reaction
        FROM posts
        WHERE id = :postId
            AND expires_at > NOW()
        ON CONFLICT (post_id, user_id)
        DO UPDATE SET reaction = EXCLUDED.reaction
        WHERE reaction IS DISTINCT FROM EXCLUDED.reaction
    """, nativeQuery = true)
    int setReaction(UUID postId, UUID userId, int reaction);
}
