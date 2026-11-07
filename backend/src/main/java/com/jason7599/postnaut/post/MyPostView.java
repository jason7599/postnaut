package com.jason7599.postnaut.post;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Synchronize;

import java.time.Instant;
import java.util.UUID;

@Entity
@Immutable
@Table(name = "my_posts")
@Synchronize({"posts", "views", "reply_chains"})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class MyPostView {

    @Id
    private UUID id;
    private UUID posterId;
    private String content;
    private boolean revealToFriends;
    private boolean replyEnabled;
    private UUID clientId;
    private Instant createdAt;
    private Instant expiresAt;
    private int viewCount;
    private int likeCount;
    private int dislikeCount;
    private int replyChainCount;
    private int unreadChainCount;
    private boolean isExpired;
}
