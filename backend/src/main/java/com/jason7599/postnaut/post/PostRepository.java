package com.jason7599.postnaut.post;

import com.jason7599.postnaut.post.dto.RandomPostResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

interface PostRepository extends JpaRepository<PostEntity, UUID> {

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

    /** No-op if the post is expired or the reaction is unchanged */
    @Modifying
    @Query(value = """
        INSERT INTO views (post_id, user_id, reaction)
        SELECT id, :userId, :reaction
        FROM posts
        WHERE id = :postId
            AND expires_at > NOW()
        ON CONFLICT (post_id, user_id)
        DO UPDATE SET reaction = EXCLUDED.reaction
        WHERE views.reaction IS DISTINCT FROM EXCLUDED.reaction
    """, nativeQuery = true)
    int setReaction(UUID postId, UUID userId, int reaction);

    @Query(value = """
        SELECT
            p.id,
            p.content,
            p.reply_enabled,
            p.created_at,
            p.expires_at
        FROM posts p
        WHERE p.expires_at > NOW()
            AND p.poster_id <> :viewerId
            AND NOT EXISTS (
                SELECT 1
                FROM views v
                WHERE v.post_id = p.id
                    AND v.user_id = :viewerId
            )
            AND (
                NOT p.reveal_to_friends
                OR NOT EXISTS (
                    SELECT 1
                    FROM friends f
                    WHERE f.owner_id = :viewerId
                        AND f.friend_id = p.poster_id -- table symmetry is enforced by fk
                )
            )
        ORDER BY random()
        LIMIT 1
    """, nativeQuery = true)
    Optional<RandomPostResponse> getRandomPost(UUID viewerId);
}
