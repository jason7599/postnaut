package com.jason7599.postnaut.reply;

import com.jason7599.postnaut.error.ApiErrorCode;
import com.jason7599.postnaut.error.ApiException;
import com.jason7599.postnaut.post.PostReplyContext;
import com.jason7599.postnaut.post.PostService;
import com.jason7599.postnaut.reply.dto.CreateReplyRequest;
import com.jason7599.postnaut.reply.dto.ReplyChainResponse;
import com.jason7599.postnaut.reply.dto.ReplyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;

    private final PostService postService;

    /**
     * Request to create a fresh new reply chain.
     * Must be called ONLY for the first reply on a post.
     * Therefore, userId must not be the poster's id.
     */
    @Transactional
    public ReplyResponse startReplyChain(
            UUID postId,
            UUID userId,
            @Valid CreateReplyRequest request
    ) {
        PostReplyContext replyContext = postService.getReplyContext(postId);
        if (!replyContext.replyEnabled()) {
            throw new ApiException(ApiErrorCode.REPLIES_DISABLED);
        }

        if (userId.equals(replyContext.posterId())) {
            throw new ApiException(ApiErrorCode.CANNOT_INTERACT_WITH_OWN_POST);
        }

        UUID chainId = replyRepository.upsertReplyChain(postId, userId);

        // *************** LOCK ***************
        ReplyChain replyChain = replyRepository.lockReplyChain(chainId)
                .orElseThrow(() -> new IllegalStateException("chain not found after upsert " + chainId));

        // Non-empty chain
        if (replyChain.lastSeq() > 0) {
            ReplyEntity existing = replyRepository.findByChainIdAndSeq(chainId, 1)
                    .orElseThrow(() -> new IllegalStateException("empty reply chain with non zero last_seq " + chainId));

            // Huh, if a user has 2 sessions, one that knows the chain exists and one that doesn't,
            // I guess this can happen. Then throwing a separate error here would make sense,
            // given appending a reply to a chain for a back and forth, and leaving a reply for the first time
            // have very different intentions
            if (!existing.getClientId().equals(request.clientId())) {
                throw new ApiException(ApiErrorCode.REPLY_CHAIN_ALREADY_EXISTS);
            }

            if (!existing.getContent().equals(request.content())) {
                throwClientIdConflictError(chainId, request.clientId());
            }

            return toReplyResponse(existing);
        }

        // As of the current design, there shouldn't be a case where it returns empty
        ReplyEntity inserted = replyRepository.appendReply(chainId, request.content(), false, request.clientId())
                .orElseThrow(() -> new IllegalStateException("appendReply failed " + chainId));

        return toReplyResponse(inserted);
    }

    @Transactional
    public ReplyResponse appendReply(
            UUID chainId,
            UUID userId,
            @Valid CreateReplyRequest request
    ) {
        // ************ LOCK ************
        ReplyChain replyChain = replyRepository.lockReplyChain(chainId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.REPLY_CHAIN_NOT_FOUND));

        PostReplyContext replyContext = postService.getReplyContext(replyChain.postId());

        boolean fromPoster = userId.equals(replyContext.posterId());

        // Again no distinguishing from non-existent, guarding against id probing
        if (!fromPoster && !userId.equals(replyChain.replierId())) {
            throw new ApiException(ApiErrorCode.REPLY_CHAIN_NOT_FOUND);
        }

        ReplyEntity existing = replyRepository.findByChainIdAndClientId(chainId, request.clientId()).orElse(null);
        if (existing != null) {
            if (existing.isFromPoster() != fromPoster || !existing.getContent().equals(request.content())) {
                throwClientIdConflictError(chainId, request.clientId());
            }

            return toReplyResponse(existing);
        }

        if (!replyContext.replyEnabled()) {
            throw new ApiException(ApiErrorCode.REPLIES_DISABLED);
        }

        if (isPosterTurn(replyChain.lastSeq()) != fromPoster) {
            throw new ApiException(ApiErrorCode.REPLY_OUT_OF_TURN);
        }

        ReplyEntity inserted = replyRepository.appendReply(chainId, request.content(), fromPoster, request.clientId())
                .orElseThrow(() -> new IllegalStateException("appendReply failed " + chainId));

        return toReplyResponse(inserted);
    }

    @Transactional(readOnly = true)
    public List<ReplyChainResponse> getReplyChainsOnMyPost(UUID postId, UUID userId) {
        // Thought about throwing POST_NOT_FOUND to guard against leakage,
        // but post_ids aren't much a secret anyway. So this just makes the intention clearer
        if (!postService.getReplyContext(postId).posterId().equals(userId)) {
            throw new ApiException(ApiErrorCode.NOT_MY_POST);
        }

        return List.of();
//        return replyRepository.getReplyChainsOnMyPost(postId);
    }

    /** Reply chains that I started */
    @Transactional(readOnly = true)
    public List<ReplyChainResponse> getReplyChainsAsReplier(UUID userId) {
//        return replyRepository.getReplyChainsAsReplier(userId);
        return List.of();
    }


    public static boolean isPosterTurn(int replySeq) {
        return replySeq % 2 == 1;
    }

    private static ReplyResponse toReplyResponse(ReplyEntity e) {
        return new ReplyResponse(
                e.getId().chainId(),
                e.getId().seq(),
                e.isFromPoster(),
                e.getContent(),
                e.getCreatedAt()
        );
    }

    private static void throwClientIdConflictError(UUID chainId, UUID clientId) {
        log.warn("clientId reused with different payload: chainId={}, clientId={}", chainId, clientId);
        throw new ApiException(ApiErrorCode.REPLY_CLIENT_ID_CONFLICT);
    }
}
