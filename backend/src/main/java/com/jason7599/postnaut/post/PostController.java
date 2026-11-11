package com.jason7599.postnaut.post;

import com.jason7599.postnaut.auth.AuthUser;
import com.jason7599.postnaut.common.PageResponse;
import com.jason7599.postnaut.post.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
class PostController {

    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    MyPostResponse create(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid CreatePostRequest request
    ) {
        return postService.createPost(authUser.userId(), request);
    }

    @GetMapping("/me/posts")
    PageResponse<MyPostResponse> getMyPosts(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "ALL") PostStatusFilter status,
            @RequestParam(defaultValue = "NEWEST") MyPostSort sort,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return postService.getUserPosts(authUser.userId(), cursor, status, sort, size);
    }

    @PutMapping("/posts/{postId}/view")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void recordView(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID postId
    ) {
        postService.recordView(postId, authUser.userId());
    }

    @PutMapping("/posts/{postId}/reaction")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void setReaction(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID postId,
            @RequestBody @Valid SetReactionRequest request
    ) {
        postService.setReaction(postId, authUser.userId(), request);
    }

    @GetMapping("/posts/random")
    ResponseEntity<RandomPostResponse> getRandom(@AuthenticationPrincipal AuthUser authUser) {
        return postService.fetchRandomPost(authUser.userId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
