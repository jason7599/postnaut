package com.jason7599.postnaut.post;

import com.jason7599.postnaut.auth.AuthUser;
import com.jason7599.postnaut.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
}
