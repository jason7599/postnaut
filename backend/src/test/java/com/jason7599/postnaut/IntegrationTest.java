package com.jason7599.postnaut;

import com.jason7599.postnaut.error.ApiErrorCode;
import com.jason7599.postnaut.support.TestClient;
import com.jason7599.postnaut.support.UserFixtures;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest         // starts the full spring application
@AutoConfigureMockMvc   // mockmvc to send fake HTTP requests without opening a real port
@Import({TestClient.class, UserFixtures.class})
public abstract class IntegrationTest {

    @ServiceConnection  // Points my app's database config at this container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @SuppressWarnings("resource")
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }

    @Autowired private TestClient testClient;

    @Autowired protected JdbcTemplate jdbc;
    @Autowired protected StringRedisTemplate redisTemplate;

    @BeforeEach
    protected void clear() {
        jdbc.execute("TRUNCATE TABLE users CASCADE");

        redisTemplate.execute((RedisCallback<Void>) conn -> {
            conn.serverCommands().flushAll();
            return null;
        });
    }

    protected ResultActions post(String path, Object body, Cookie cookie) throws Exception {
        return testClient.post(path, body, cookie);
    }

    protected ResultActions get(String path, Cookie cookie) throws Exception {
        return testClient.get(path, cookie);
    }

    protected static ResultMatcher apiError(ApiErrorCode code) {
        return result -> {
            status().is(code.getStatus().value()).match(result);
            jsonPath("$.code").value(code.name()).match(result);
        };
    }
}
