package com.jason7599.postnaut.post;

import com.jason7599.postnaut.error.ApiErrorCode;
import com.jason7599.postnaut.error.ApiException;
import com.jason7599.postnaut.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    private final UserService userService;

    @Transactional
    public MyPostResponse createPost(UUID userId, CreatePostRequest request) {
        // transaction level lock to ensure live post cap
        userService.lock(userId);

        PostEntity existing = postRepository.findByPosterIdAndClientId(userId, request.clientId()).orElse(null);
        if (existing != null) {
            return new MyPostResponse(existing);
        }

        Instant now = Instant.now();
        long liveCount = postRepository.countByPosterIdAndExpiresAtAfter(userId, now);
        if (liveCount >= PostConstraints.MAX_LIVE_POSTS) {
            if (liveCount > PostConstraints.MAX_LIVE_POSTS) {
                log.error("User {} has {} live posts", userId, liveCount);
            }
            throw new ApiException(ApiErrorCode.MAX_LIVE_POST_REACHED);
        }

        return new MyPostResponse(
            postRepository.insertIfAbsent(
                userId,
                request.content(),
                request.revealToFriends(),
                request.replyEnabled(),
                request.clientId(),
                now.plus(request.lifespan())
            ).orElseThrow(() -> new IllegalStateException(
                "Conflict on (poster_id, client_id) after lookup under lock, poster: " + userId
            ))
        );
    }
}
