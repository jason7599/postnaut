package com.jason7599.postnaut.reply;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "replies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ReplyEntity {

    @Embeddable
    record Id(
            UUID chainId,
            int seq
    ) {}

    @EmbeddedId
    private Id id;

    private boolean fromPoster;

    private String content;

    private UUID clientId;

    @CreationTimestamp
    private Instant createdAt;
}
