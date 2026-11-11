package com.jason7599.postnaut.reply;

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
@Table(name = "reply_chains_view")
@Synchronize({"reply_chains", "posts", "replies", "friends", "users"})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ReplyChainView {

    @Id
    private UUID id;
    private UUID postId;
    private UUID replierId;
    private String postContent;
    private Instant postExpiresAt;
    private int lastSeq;
    private boolean isPosterTurn;
    private boolean posterHasUnread;
    private boolean replierHasUnread;
    private String replierNameForPoster;
    private String posterNameForReplier;
    private String lastReplyContent;
    private Instant lastReplyAt;
}
