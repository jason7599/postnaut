package com.jason7599.postnaut.post;

import com.jason7599.postnaut.auth.AuthUser;
import com.jason7599.postnaut.common.PageResponse;
import com.jason7599.postnaut.post.dto.CreatePostRequest;
import com.jason7599.postnaut.post.dto.MyPostResponse;
import com.jason7599.postnaut.post.dto.RandomPostResponse;
import com.jason7599.postnaut.post.dto.SetReactionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/posts")
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

    @GetMapping("/mine")
    PageResponse<MyPostResponse> getMyPosts(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(required = false) UUID cursor,
            @RequestParam(defaultValue = "ALL") PostStatusFilter filter
    ) {
        return postService.getUserPosts(authUser.userId(), cursor, filter);
    }

    @PutMapping("/{postId}/view")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void recordView(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID postId
    ) {
        postService.recordView(postId, authUser.userId());
    }

    @PutMapping("/{postId}/reaction")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void setReaction(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable UUID postId,
            @RequestBody @Valid SetReactionRequest request
    ) {
        postService.setReaction(postId, authUser.userId(), request);
    }

    @GetMapping("/random")
    ResponseEntity<RandomPostResponse> getRandom(@AuthenticationPrincipal AuthUser authUser) {
        return postService.fetchRandomPost(authUser.userId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
