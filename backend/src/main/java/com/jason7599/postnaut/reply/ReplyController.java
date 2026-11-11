package com.jason7599.postnaut.reply;

import com.jason7599.postnaut.auth.AuthUser;
import com.jason7599.postnaut.reply.dto.CreateReplyRequest;
import com.jason7599.postnaut.reply.dto.PosterReplyChainResponse;
import com.jason7599.postnaut.reply.dto.ReplierReplyChainResponse;
import com.jason7599.postnaut.reply.dto.ReplyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
class ReplyController {

    private final ReplyService replyService;

    @PostMapping("/posts/{postId}/reply-chains")
    @ResponseStatus(HttpStatus.CREATED)
    ReplyResponse startReplyChain(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID postId,
            @Valid @RequestBody CreateReplyRequest request
    ) {
        return replyService.startReplyChain(postId, authUser.userId(), request);
    }

    @PostMapping("/reply-chains/{chainId}/replies")
    @ResponseStatus(HttpStatus.CREATED)
    ReplyResponse appendReply(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID chainId,
            @Valid @RequestBody CreateReplyRequest request
    ) {
        return replyService.appendReply(chainId, authUser.userId(), request);
    }

    @GetMapping("/posts/{postId}/reply-chains")
    List<PosterReplyChainResponse> getReplyChainsOnMyPost(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID postId
    ) {
        return replyService.getReplyChainsOnMyPost(postId, authUser.userId());
    }

    @GetMapping("/me/reply-inbox")
    List<ReplierReplyChainResponse> getReplierInbox(@AuthenticationPrincipal AuthUser authUser) {
        return replyService.getReplierInbox(authUser.userId());
    }
}
