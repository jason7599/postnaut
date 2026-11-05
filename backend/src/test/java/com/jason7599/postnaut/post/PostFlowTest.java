package com.jason7599.postnaut.post;

import com.jason7599.postnaut.IntegrationTest;
import com.jason7599.postnaut.common.PageResponse;
import com.jason7599.postnaut.error.ApiErrorCode;
import com.jason7599.postnaut.support.UserFixtures;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.type.TypeReference;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import static com.jason7599.postnaut.support.UserFixtures.TestUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
class PostFlowTest extends IntegrationTest {

    @Autowired UserFixtures users;

    @Test
    void createPost_valid_createsPost() throws Exception {
        TestUser user = users.create("jason");

        createPost(user.session());

        assertPostCount(1);
    }

    @Test
    void createPost_duplicatePosterIdAndClientId_retrievesExisting() throws Exception {
        TestUser user = users.create("jason");

        UUID clientId = UUID.randomUUID();

        for (int i = 0; i < 5; i++) {
            createPost(clientId, user.session());
        }

        assertPostCount(1);
    }

    @Test
    void createPost_duplicateClientIdButDifferentPosterId_doesntCollide() throws Exception {
        TestUser user1 = users.create("jason");
        TestUser user2 = users.create("jux");

        UUID clientId = UUID.randomUUID();

        createPost(clientId, user1.session());

        createPost(clientId, user2.session());

        assertPostCount(2);
    }

    @Test
    void createPost_reachedLiveCap_fails() throws Exception {
        TestUser user = users.create("jason");

        for (int i = 0; i < PostConstraints.MAX_LIVE_POSTS; i++) {
            createPost(user.session());
        }

        for (int i = 0; i < 5; i++) {
            createPost(user.session())
                    .andExpect(apiError(ApiErrorCode.MAX_LIVE_POST_REACHED));
        }

        assertPostCount(PostConstraints.MAX_LIVE_POSTS);
    }

    @RepeatedTest(50)
    void createPost_stress_neverExceedsLiveCap() throws Exception {
        TestUser user = users.create("jason");
        final int requests = PostConstraints.MAX_LIVE_POSTS * 3;

        // CountDownLatch: A gate that opens when a counter reaches zero
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();

        try (ExecutorService executor = Executors.newFixedThreadPool(requests)) {
            // Queue tasks
            for (int i = 0; i < requests; i++) {
                futures.add(executor.submit(() -> {
                    // every thread waits here until released together by the latch
                    start.await();

                    return createPost(user.session())
                            .andReturn().getResponse().getStatus();
                }));
            }

            start.countDown();

            for (Future<Integer> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        }

        assertPostCount(PostConstraints.MAX_LIVE_POSTS);
    }

    @Test
    void getUserPosts_valid_normal() throws Exception {
        TestUser user = users.create("jason");
        int postCount = PostConstraints.MAX_LIVE_POSTS;

        for (int i = 0; i < postCount; i++) {
            createPost(String.valueOf(i), user.session())
                    .andExpect(status().isCreated());
        }

        var res = getUserPosts(user.session(), null, null)
                .andExpect(status().isOk());

        PageResponse<MyPostResponse> page = read(res, new TypeReference<>() {});

        assertThat(page.items()).hasSize(postCount);
        assertThat(page.hasMore()).isFalse();

        for (int i = 0; i < postCount; i++) {
            String expected = String.valueOf(postCount - 1 - i);
            assertThat(page.items().get(i).content()).isEqualTo(expected);
        }
    }

    private void assertPostCount(int count) {
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM posts", Integer.class)).isEqualTo(count);
    }

    private ResultActions createPost(String content, UUID clientId, Duration lifespan, Cookie session) throws Exception {
        return post(
                "/posts",
                new CreatePostRequest(
                        content,
                        true,
                        true,
                        clientId,
                        lifespan
                ),
                session
        );
    }

    private ResultActions createPost(UUID clientId, Cookie session) throws Exception {
        return createPost("hello", clientId, Duration.ofDays(1), session);
    }

    private ResultActions createPost(Cookie session) throws Exception {
        return createPost(UUID.randomUUID(), session);
    }

    private ResultActions createPost(String content, Duration lifespan, Cookie session) throws Exception {
        return createPost(content, UUID.randomUUID(), lifespan, session);
    }

    private ResultActions createPost(String content, Cookie session) throws Exception {
        return createPost(content, Duration.ofDays(1), session);
    }

    private ResultActions getUserPosts(
            Cookie session,
            UUID cursor,
            PostStatusFilter filter
    ) throws Exception {
        String path = UriComponentsBuilder.fromPath("/posts/mine")
                .queryParamIfPresent("cursor", Optional.ofNullable(cursor))
                .queryParamIfPresent("status", Optional.ofNullable(filter))
                .toUriString();

        return get(path, session);
    }
}
