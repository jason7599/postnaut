package com.jason7599.postnaut.post;

import org.springframework.data.jpa.repository.JpaRepository;
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
    Optional<PostEntity> insertIfAbsent(
            UUID posterId,
            String content,
            boolean revealToFriends,
            boolean replyEnabled,
            UUID clientId,
            Instant expiresAt
    );

    long countByPosterIdAndExpiresAtAfter(UUID posterId, Instant after);

    Optional<PostEntity> findByPosterIdAndClientId(UUID posterId, UUID clientId);
}
