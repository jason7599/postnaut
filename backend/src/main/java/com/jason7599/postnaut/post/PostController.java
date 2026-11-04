package com.jason7599.postnaut.post;

import com.jason7599.postnaut.auth.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
