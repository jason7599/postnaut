package com.jason7599.postnaut.post;

import com.jason7599.postnaut.IntegrationTest;
import com.jason7599.postnaut.support.UserFixtures;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.util.UUID;

import static com.jason7599.postnaut.support.UserFixtures.TestUser;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class PostFlowTest extends IntegrationTest {

    @Autowired UserFixtures users;

    @Test
    void createPost_valid_createsPost() throws Exception {
        TestUser user = users.create("jason");

        createPost(UUID.randomUUID(), user.session());

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

}
