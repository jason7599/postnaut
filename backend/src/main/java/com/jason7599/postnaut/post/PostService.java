package com.jason7599.postnaut.post;

import com.jason7599.postnaut.common.PageResponse;
import com.jason7599.postnaut.error.ApiErrorCode;
import com.jason7599.postnaut.error.ApiException;
import com.jason7599.postnaut.post.dto.*;
import com.jason7599.postnaut.user.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// @Validated:
// Validation on the Service level too, not just on the controller. Kinda defensive
// given nothing else calls the service classes other than the controller class, but why not.
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MyPostViewRepository myPostViewRepository;

    private final UserService userService;

    @Transactional
    public MyPostResponse createPost(UUID userId, @NotNull @Valid CreatePostRequest request) {
        // transaction level lock to ensure live post cap
        // Verified that without this lock, write skews actually happen!
        userService.lock(userId);

        MyPostView existing = myPostViewRepository.findByPosterIdAndClientId(userId, request.clientId()).orElse(null);
        if (existing != null) return toMyPostResponse(existing);

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

    public PageResponse<MyPostResponse> getUserPosts(UUID userId, UUID cursor, PostStatusFilter status, MyPostSort myPostSort, int pageSize) {
        ScrollPosition pos = cursor == null
                ? ScrollPosition.keyset() // initial scroll
                : ScrollPosition.forward(Map.of("id", cursor));

        Sort sort = myPostSort == MyPostSort.NEWEST
                ? Sort.by("id").descending()
                : Sort.by("id").ascending();

        Limit limit = Limit.of(pageSize);

        Window<MyPostView> window;

        if (status == PostStatusFilter.ALL) window = myPostViewRepository.findByPosterId(userId, pos, sort, limit);
        else {
            window = myPostViewRepository.findByPosterIdAndIsExpired(userId, status == PostStatusFilter.EXPIRED, pos, sort, limit);
        }

        return PageResponse.fromWindow(
                window,
                this::toMyPostResponse,
                MyPostView::getId
        );
    }

    @Transactional
    public void recordView(UUID postId, UUID userId) {
        if (requireLivePost(postId).getPosterId().equals(userId)) {
            throw new ApiException(ApiErrorCode.CANNOT_INTERACT_WITH_OWN_POST);
        }

        // Technically TOCTOU. Post might've been deleted or expired after the check above
        // But still safe, as the insertion no-ops in those cases too.
        if (postRepository.insertView(postId, userId) != 0) {
            // If this matters later.
        }
    }

    @Transactional
    public void setReaction(UUID postId, UUID userId, @NotNull @Valid SetReactionRequest request) {
        if (requireLivePost(postId).getPosterId().equals(userId)) {
            throw new ApiException(ApiErrorCode.CANNOT_INTERACT_WITH_OWN_POST);
        }

        // Again, TOCTOU, but same as above. Nothing breaks.
        if (postRepository.setReaction(postId, userId, request.reaction().getReactionvalue()) != 0) {
            // If this matters later.
        }
    }

    public Optional<RandomPostResponse> fetchRandomPost(UUID viewerId) {
        return postRepository.getRandomPost(viewerId);
    }

    public void requireOwnPost(UUID postId, UUID userId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.POST_NOT_FOUND));

        if (!post.getPosterId().equals(userId)) {
            throw new ApiException(ApiErrorCode.NOT_MY_POST);
        }
    }

    public PostReplyContext getReplyContext(UUID postId) {
        PostEntity post = requireLivePost(postId);
        return new PostReplyContext(
                post.getPosterId(),
                post.isReplyEnabled()
        );
    }

    private PostEntity requireLivePost(UUID postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.POST_NOT_FOUND));

        // No need to distinguish an expired post from a non-existent one.
        if (post.isExpired()) {
            throw new ApiException(ApiErrorCode.POST_NOT_FOUND);
        }

        return post;
    }

    private MyPostResponse toMyPostResponse(MyPostView v) {
        return new MyPostResponse(
                v.getId(),
                v.getContent(),
                v.isRevealToFriends(),
                v.isReplyEnabled(),
                v.getCreatedAt(),
                v.getExpiresAt(),
                v.getViewCount(),
                v.getLikeCount(),
                v.getDislikeCount(),
                v.getReplyChainCount(),
                v.getReplyChainCount(),
                v.isExpired()
        );
    }
}
