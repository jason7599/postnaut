package com.jason7599.postnaut.post;

import com.jason7599.postnaut.common.PageResponse;
import com.jason7599.postnaut.error.ApiErrorCode;
import com.jason7599.postnaut.error.ApiException;
import com.jason7599.postnaut.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// @Validated:
// Validation on the Service level too, not just on the controller. Kinda defensive
// given nothing else calls the service classes other than the controller class, but why not.
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class PostService {

    private static final int MY_POSTS_PAGE_SIZE = 50;

    private final PostRepository postRepository;

    private final UserService userService;

    @Transactional
    public MyPostResponse createPost(UUID userId, @NotNull @Valid CreatePostRequest request) {
        // transaction level lock to ensure live post cap
        // Verified that without this lock, write skews actually happen!
        userService.lock(userId);

        MyPostResponse existing = postRepository.getUserPostByPosterIdAndClientId(userId, request.clientId()).orElse(null);
        if (existing != null) return existing;

        Instant now = Instant.now();
        long liveCount = postRepository.countByPosterIdAndExpiresAtAfter(userId, now);
        if (liveCount >= PostConstraints.MAX_LIVE_POSTS) {
            // Shouldn't happen thanks to the lock
            if (liveCount > PostConstraints.MAX_LIVE_POSTS) {
                log.error("User {} has {} live posts", userId, liveCount);
            }
            throw new ApiException(ApiErrorCode.MAX_LIVE_POST_REACHED);
        }

        PostEntity inserted = postRepository.insertPost(
                userId,
                request.content(),
                request.revealToFriends(),
                request.replyEnabled(),
                request.clientId(),
                now.plus(request.lifespan())
        ).orElseThrow(() -> new IllegalStateException(
                "Conflict on (poster_id, client_id) after lookup under lock, poster: " + userId
        ));

        return new MyPostResponse(
                inserted.getId(),
                inserted.getContent(),
                inserted.isRevealToFriends(),
                inserted.isReplyEnabled(),
                inserted.getCreatedAt(),
                inserted.getExpiresAt(),
                0,
                0,
                0,
                0,
                0,
                false
        );
    }

    public PageResponse<MyPostResponse> getUserPosts(UUID userId, UUID cursor, PostStatusFilter filter) {
        // null cursor: initial fetch. Assign max uuid
        if (cursor == null) cursor = new UUID(-1L, -1L);

        List<MyPostResponse> items = postRepository.getUserPosts(
                userId,
                cursor,
                filter != PostStatusFilter.EXPIRED,
                filter != PostStatusFilter.LIVE,
                MY_POSTS_PAGE_SIZE + 1
        );

        boolean hasMore = items.size() > MY_POSTS_PAGE_SIZE;
        if (hasMore) {
            items = items.subList(0, MY_POSTS_PAGE_SIZE);
        }

        return new PageResponse<>(items, hasMore);
    }

    @Transactional
    public void recordView(UUID postId, UUID userId) {
        // No need to distinguish an expired post from a non-existent one
        if (!postRepository.isPostActive(postId)) {
            throw new ApiException(ApiErrorCode.POST_NOT_FOUND);
        }

        // Technically TOCTOU. Post might've been deleted or expired after the check above
        // But still safe, as the insertion no-ops in those cases too.
        if (postRepository.insertView(postId, userId) != 0) {
            // If this matters later.
        }
    }

    @Transactional
    public void setReaction(UUID postId, UUID userId, @NotNull @Valid SetReactionRequest request) {
        if (!postRepository.isPostActive(postId)) {
            throw new ApiException(ApiErrorCode.POST_NOT_FOUND);
        }

        // Again, TOCTOU, but same as above. Nothing breaks.
        if (postRepository.setReaction(postId, userId, request.reaction().getReactionvalue()) != 0) {
            // If this matters later.
        }
    }
}
