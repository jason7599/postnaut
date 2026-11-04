package com.jason7599.postnaut.post;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class PostEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    private UUID posterId;

    private String content;

    private boolean revealToFriends;

    private boolean replyEnabled;

    private UUID clientId;

    @CreationTimestamp
    private Instant createdAt;

    private Instant expiresAt;

    private int viewCount;
    private int likeCount;
    private int dislikeCount;
}
